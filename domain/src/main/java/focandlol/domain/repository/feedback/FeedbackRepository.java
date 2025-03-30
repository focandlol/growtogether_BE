package focandlol.domain.repository.feedback;

import focandlol.domain.entity.feedback.FeedbackEntity;
import focandlol.domain.entity.join.StudyMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
  boolean existsByStudyMember(StudyMemberEntity studyMember);
}
