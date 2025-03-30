package focandlol.domain.repository.feedback;


import focandlol.domain.entity.feedback.FeedbackContentEntity;
import focandlol.domain.entity.join.StudyMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackContentRepository extends JpaRepository<FeedbackContentEntity, Long> {

  Long countByStudyMember(StudyMemberEntity studyMemberEntity);

}
