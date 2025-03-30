package focandlol.api.study.service.vote;

import static focandlol.common.exception.response.ErrorCode.*;
import static focandlol.domain.type.StudyMemberType.LEADER;
import static focandlol.domain.type.StudyMemberType.NORMAL;
import static focandlol.domain.type.VoteStatus.COMPLETE;
import static focandlol.domain.type.VoteStatus.PROGRESS;

import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.dto.study.schedule.ScheduleUpdateDto;
import focandlol.domain.dto.study.vote.ChangeVoteDetailsDto;
import focandlol.domain.dto.study.vote.KickVoteDetailsDto;
import focandlol.domain.dto.study.vote.VoteCreateDto;
import focandlol.domain.dto.study.vote.VoteDto;
import focandlol.domain.dto.study.vote.VotingDto;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.entity.schedule.ScheduleEntity;
import focandlol.domain.entity.vote.ChangeVoteEntity;
import focandlol.domain.entity.vote.KickVoteEntity;
import focandlol.domain.entity.vote.VoteEntity;
import focandlol.domain.entity.vote.VotingEntity;
import focandlol.domain.repository.join.JoinRepository;
import focandlol.domain.repository.vote.ChangeVoteRepository;
import focandlol.domain.repository.vote.KickVoteRepository;
import focandlol.domain.repository.vote.VoteRepository;
import focandlol.domain.repository.vote.VotingRepository;
import jakarta.persistence.DiscriminatorValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VoteService {

  private final JoinRepository joinRepository;
  private final VoteRepository voteRepository;
  private final SchedulerService schedulerService;
  private final VotingRepository votingRepository;
  private final KickVoteRepository kickVoteRepository;
  private final ChangeVoteRepository changeVoteRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final VoteProcessorFactory voteProcessorFactory;
  private final Scheduler scheduler;

  public void createKickVote(Long memberId, Long studyId, VoteCreateDto voteCreateDto) {

    StudyMemberEntity studyMemberEntity = getStudyMemberEntity(memberId, studyId);

    StudyMemberEntity voted = joinRepository.findById(voteCreateDto.getStudyMemberId())
        .orElseThrow(() -> new CustomException(NOT_A_STUDY_MEMBER));

    String title = voted.getMember().getNickName() + "님의 대한 추방 투표입니다.";

    KickVoteEntity save = kickVoteRepository.save(
        KickVoteEntity.create(title, studyMemberEntity, voted));

    settingVote(studyId, save.getId());

    scheduleJob(KickVoteJob.class, "job"+save.getId(), "job", 10, save.getId());
  }

  public void createChangeVote(Long memberId, Long studyId, ScheduleEntity scheduleEntity,
      ScheduleUpdateDto scheduleUpdateDto) {
    StudyMemberEntity studyMemberEntity = getStudyMemberEntity(memberId, studyId);

    String title = scheduleEntity.getTitle() + " 스케줄 시간 변경 투표입니다.";

    ChangeVoteEntity save = changeVoteRepository.save(
        ChangeVoteEntity.create(title, studyMemberEntity, scheduleUpdateDto, scheduleEntity));

    settingVote(studyId, save.getId());

    scheduleJob(ChangeVoteJob.class, "job"+save.getId(), "job", 10, save.getId());
  }

  private void scheduleJob(Class<? extends Job> jobClass, String jobName, String jobGroup,
      long delayMinutes, Long voteId) {
    Map<String, Object> data = new HashMap<>();
    data.put("id", voteId);

    schedulerService.scheduleJob(jobClass, jobName, jobGroup, delayMinutes, data);
  }

  public void vote(Long memberId, Long voteId, VotingDto votingDto) {
    VoteEntity voteEntity = voteRepository.findById(voteId)
        .orElseThrow(() -> new CustomException(VOTE_NOT_FOUND));

    StudyMemberEntity studyMemberEntity = getStudyMemberEntity(memberId,
        voteEntity.getStudy().getStudyId());

    validateVote(voteEntity, studyMemberEntity);

    votingRepository.save(VotingEntity.create(voteEntity, studyMemberEntity));

    saveInRedis(voteEntity, votingDto);
  }

  private StudyMemberEntity getStudyMemberEntity(Long memberId, Long studyId) {
    return joinRepository.findByMember_MemberIdAndStudy_StudyIdAndStatusIn(memberId, studyId,
            List.of(LEADER, NORMAL))
        .orElseThrow(() -> new CustomException(NOT_A_STUDY_MEMBER));
  }

  private void validateVote(VoteEntity voteEntity, StudyMemberEntity studyMemberEntity) {
    if (!LEADER.equals(studyMemberEntity.getStatus()) && !NORMAL.equals(
        studyMemberEntity.getStatus())) {
      throw new CustomException(STUDY_MEMBER_ONLY);
    }

    if (votingRepository.existsByVoteAndStudyMember(voteEntity, studyMemberEntity)) {
      throw new CustomException(VOTING_ALREADY_EXISTS);
    }

    if (COMPLETE.equals(voteEntity.getStatus())) {
      throw new CustomException(VOTE_ALREADY_COMPLETE);
    }
  }

  private void settingVote(Long studyId, Long voteId) {
    long count = joinRepository.countByStudy_StudyIdAndStatusIn(studyId, List.of(LEADER, NORMAL));
    redisTemplate.opsForHash().increment("vote" + voteId, "size", count);
  }

  private void saveInRedis(VoteEntity voteEntity, VotingDto votingDto) {
    String key = "vote" + voteEntity.getId();
    Long agree = 0L;
    if (votingDto.isAgree()) {
      agree = redisTemplate.opsForHash().increment(key, "agree", 1);
    }
    Long total = redisTemplate.opsForHash().increment(key, "total", 1);

    Long size = Optional.ofNullable(redisTemplate.opsForHash().get(key, "size"))
        .map(obj -> ((Number) obj).longValue())
        .orElse(0L);

    if(total >= size){
      log.info("즉시 실행");
      sumKickVote(voteEntity.getId());
      return;
    }

    if(voteEntity.getClass().getAnnotation(DiscriminatorValue.class).value().equals("KICK")){
      if(agree > size / 2){
        log.info("강퇴 즉시 실행");
        sumKickVote(voteEntity.getId());
      }
    }
  }

  public void sumKickVote(Long voteId) {
    VoteEntity voteEntity = voteRepository.findVoteAndStudyById(voteId)
        .orElseThrow(() -> new CustomException(VOTE_NOT_FOUND));

    String key = "vote" + voteId;

    Long size = Optional.ofNullable(redisTemplate.opsForHash().get(key, "size"))
        .map(obj -> ((Number) obj).longValue())
        .orElse(0L);

    Long agree = Optional.ofNullable(redisTemplate.opsForHash().get(key, "agree"))
        .map(obj -> ((Number) obj).longValue())
        .orElse(0L);

    VoteProcessor processor = voteProcessorFactory.getProcessor(voteEntity.getClass());

    if (size > 0) {
        processor.processVote(voteEntity, agree, size);
    }
    voteEntity.setStatus(COMPLETE);
    redisTemplate.delete("vote" + voteId);

    try {
      scheduler.deleteJob(new JobKey("job" + voteEntity.getId(), "job"));
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  public List<VoteDto> getVotes(Long studyId) {
    return voteRepository.findByStudy_StudyIdAndStatus(studyId,
            PROGRESS).stream()
        .map(v -> VoteDto.fromEntity(v))
        .collect(Collectors.toList());
  }

  public VoteDto getDetailVote(Long voteId){
    VoteEntity voteEntity = voteRepository.findById(voteId)
        .orElseThrow(() -> new CustomException(VOTE_NOT_FOUND));

    if (voteEntity instanceof KickVoteEntity kick) {
      return KickVoteDetailsDto.fromEntity(kick);
    } else if (voteEntity instanceof ChangeVoteEntity change) {
      return ChangeVoteDetailsDto.fromEntity(change);
    } else {
      throw new IllegalArgumentException("Unknown vote type");
    }


  }


}
