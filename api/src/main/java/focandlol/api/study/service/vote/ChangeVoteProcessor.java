package focandlol.api.study.service.vote;

import static focandlol.domain.type.NotiType.VOTE;
import static focandlol.domain.type.StudyMemberType.LEADER;
import static focandlol.domain.type.StudyMemberType.NORMAL;

import focandlol.api.notification.service.NotificationService;
import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.entity.schedule.ScheduleEntity;
import focandlol.domain.entity.vote.ChangeVoteEntity;
import focandlol.domain.entity.vote.VoteEntity;
import focandlol.domain.repository.join.JoinRepository;
import focandlol.domain.repository.schedule.ScheduleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChangeVoteProcessor implements VoteProcessor {

  private final ScheduleRepository scheduleRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final NotificationService notificationService;
  private final JoinRepository joinRepository;

  @Override
  @Transactional
  public void processVote(VoteEntity voteEntity, Long votes, Long totalSize) {
    ChangeVoteEntity changeVoteEntity = (ChangeVoteEntity) voteEntity;

    List<StudyMemberEntity> memberList = joinRepository.findByStudyAndStatusIn(
        changeVoteEntity.getStudy(), List.of(LEADER, NORMAL));

    if (votes >= totalSize) {

      log.info("CHANGE 투표 통과: " + changeVoteEntity.getStart());
      log.info("시간 변경: " + changeVoteEntity.getEnd());

      ScheduleEntity scheduleEntity = scheduleRepository.findById(changeVoteEntity.getSchedule().getId())
          .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

      scheduleEntity.setTitle(changeVoteEntity.getContent());
      scheduleEntity.setStart(changeVoteEntity.getStart());
      scheduleEntity.setEnd(changeVoteEntity.getEnd());
      scheduleEntity.setTotalTime(changeVoteEntity.getTotal());

      memberList.stream()
          .map(studyMemberEntity -> studyMemberEntity.getMember())
          .forEach(member -> notificationService.sendNotification(member, "메인 일정이 변경되었습니다.", null,
              VOTE));
    }else{
      memberList.stream()
          .map(studyMemberEntity -> studyMemberEntity.getMember())
          .forEach(member -> notificationService.sendNotification(member, "메인 일정 변경 투표가 부결되었습니다.", null, VOTE));
    }
    redisTemplate.delete("vote" + voteEntity.getId());
  }
}
