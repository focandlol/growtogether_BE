package focandlol.domain.repository.comment;

import focandlol.domain.entity.StudyComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudyCommentRepository extends JpaRepository<StudyComment, Long> {

    @Query("SELECT c FROM StudyComment c " +
            "WHERE c.studyId = :studyId AND c.parentCommentId = 0 AND c.studyCommentId < :lastIdx " +
            "ORDER BY c.studyCommentId DESC")
    Page<StudyComment> findByStudyIdAndIdLessThan(Long studyId, Long lastIdx, Pageable pageable);

    @Query("SELECT c FROM StudyComment c " +
            "WHERE c.studyId = :studyId AND c.parentCommentId = 0 " +
            "ORDER BY c.studyCommentId DESC")
    Page<StudyComment> findByStudyId(Long studyId, Pageable pageable);

    List<StudyComment> findByParentCommentIdOrderByCreatedAtDesc(Long parentCommentId);

    Integer countAllByStudyId(Long studyId);
}
