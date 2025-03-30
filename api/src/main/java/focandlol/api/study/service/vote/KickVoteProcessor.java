package focandlol.api.study.service.vote;

import static focandlol.domain.type.NotiType.VOTE;
import static focandlol.domain.type.StudyMemberType.KICK;
import static focandlol.domain.type.StudyMemberType.LEADER;
import static focandlol.domain.type.StudyMemberType.NORMAL;

import focandlol.api.notification.service.NotificationService;
import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.entity.vote.KickVoteEntity;
import focandlol.domain.entity.vote.VoteEntity;
import focandlol.domain.repository.join.JoinRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class KickVoteProcessor implements VoteProcessor {

  private final JoinRepository joinRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final NotificationService notificationService;

  @Override
  @Transactional
  public void processVote(VoteEntity voteEntity, Long votes, Long totalSize) {
    KickVoteEntity kickVoteEntity = (KickVoteEntity) voteEntity;
    if (votes > totalSize / 2) {
      //fetchJoin으로 바꿀것
      StudyMemberEntity studyMemberEntity = joinRepository.findById(kickVoteEntity.getTarget().getId())
          .orElseThrow(() -> new CustomException(ErrorCode.NOT_A_STUDY_MEMBER));

      studyMemberEntity.setStatus(KICK);
      log.info("KICK 투표 통과: " + studyMemberEntity.getId() + " 강퇴됨");

      notificationService.sendNotification(studyMemberEntity.getMember(),kickVoteEntity.getTarget().getMember().getNickName() + "님이 강퇴 되었습니다.", null,VOTE);
    }else{
      joinRepository.findByStudyAndStatusIn(kickVoteEntity.getStudy(), List.of(LEADER, NORMAL))
          .stream()
          .map(studyMemberEntity -> studyMemberEntity.getMember())
          .forEach(member -> notificationService.sendNotification(member, "강퇴 투표가 부결되었습니다.", null, VOTE));
    }

    redisTemplate.delete("vote" + voteEntity.getId());
  }
}
