package focandlol.api.study.service.feedback;


import static focandlol.common.exception.response.ErrorCode.ALREADY_FEEDBACK;
import static focandlol.common.exception.response.ErrorCode.INVALID_FEEDBACK_PERIOD;
import static focandlol.common.exception.response.ErrorCode.NOT_A_STUDY_MEMBER;
import static focandlol.domain.type.StudyMemberType.LEADER;
import static focandlol.domain.type.StudyMemberType.NORMAL;
import static focandlol.domain.type.StudyStatus.COMPLETE;

import focandlol.common.exception.custom.CustomException;
import focandlol.domain.dto.study.feedback.FeedbackCreateDto;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.feedback.FeedbackContentEntity;
import focandlol.domain.entity.feedback.FeedbackEntity;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.repository.feedback.FeedbackContentRepository;
import focandlol.domain.repository.feedback.FeedbackRepository;
import focandlol.domain.repository.join.JoinRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {

  private final FeedbackRepository feedbackRepository;
  private final FeedbackContentRepository feedbackContentRepository;
  private final JoinRepository joinRepository;
  private final RedissonClient redissonClient;

  public void feedback(Long memberId, Long studyId, List<FeedbackCreateDto> feedbacks) {
    StudyMemberEntity studyMemberEntity = getStudyMemberEntity(memberId, studyId);

    validationFeedback(studyMemberEntity);

    FeedbackEntity save = saveFeedback(studyMemberEntity);

    Map<Long, StudyMemberEntity> studyMemberMap = extractStudyMemberMap(feedbacks);

    saveFeedbacks(feedbacks, save, studyMemberMap);

    averageRating(feedbacks, studyMemberMap);
  }

  private Map<Long, StudyMemberEntity> extractStudyMemberMap(List<FeedbackCreateDto> feedbacks) {
    List<Long> ids = feedbacks.stream()
        .map(feedbackCreateDto -> feedbackCreateDto.getStudyMemberId())
        .collect(Collectors.toList());

    List<StudyMemberEntity> studyMembers = joinRepository.findAllWithMembersInIds(ids);

    return studyMembers.stream()
        .collect(Collectors.toMap(studyMemberEntity -> studyMemberEntity.getId(), s -> s));
  }

  private FeedbackEntity saveFeedback(StudyMemberEntity studyMemberEntity) {
    return feedbackRepository.save(FeedbackEntity.builder()
        .studyMember(studyMemberEntity)
        .build());
  }

  private StudyMemberEntity getStudyMemberEntity(Long memberId, Long studyId) {
    return joinRepository.findByStudyAndMemberWithStudyInStatus(
            studyId, memberId, List.of(NORMAL, LEADER))
        .orElseThrow(() -> new CustomException(NOT_A_STUDY_MEMBER));
  }

  private void saveFeedbacks(List<FeedbackCreateDto> feedbacks, FeedbackEntity save,
      Map<Long, StudyMemberEntity> studyMemberMap) {
    List<FeedbackContentEntity> entities = feedbacks.stream()
        .map(dto -> FeedbackContentEntity.builder()
            .feedback(save)
            .studyMember(studyMemberMap.get(dto.getStudyMemberId()))
            .content(dto.getContent())
            .score(dto.getScore())
            .build())
        .collect(Collectors.toList());

    feedbackContentRepository.saveAll(entities);
  }

  private void validationFeedback(StudyMemberEntity studyMemberEntity) {
    if(!COMPLETE.equals(studyMemberEntity.getStudy().getStudyStatus())){
      throw new CustomException(INVALID_FEEDBACK_PERIOD);
    }

    if(feedbackRepository.existsByStudyMember(studyMemberEntity)){
      throw new CustomException(ALREADY_FEEDBACK);
    }
  }

  private void averageRating(List<FeedbackCreateDto> feedbacks,
      Map<Long, StudyMemberEntity> studyMemberMap) {
    for (FeedbackCreateDto feedback : feedbacks) {
      StudyMemberEntity studyMember = studyMemberMap.get(feedback.getStudyMemberId());

      String lockKey = "feedback" + studyMember;
      RLock lock = redissonClient.getLock(lockKey);

      boolean isLocked = false;
      try {
        isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
        if (isLocked) {
          MemberEntity member = studyMember.getMember();

          Long count = feedbackContentRepository.countByStudyMember(studyMember);

          member.setRating((member.getRating() * (count + 1) + feedback.getScore()) / (count + 2));
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        if (isLocked) {
          lock.unlock();
        }
      }
    }
  }


}
