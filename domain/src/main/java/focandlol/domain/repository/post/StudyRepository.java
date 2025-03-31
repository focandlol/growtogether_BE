package focandlol.domain.repository.post;

import focandlol.domain.entity.Study;
import focandlol.domain.type.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {
    List<Study> findByStudyStatus(StudyStatus studyStatus);

    @Query("SELECT s FROM Study s " +
            "ORDER BY s.viewCount * 1 +" +
            "((SELECT COUNT(c.studyCommentId) FROM StudyComment c WHERE c.studyId = s.studyId)) * 3 +" +
            "((SELECT COUNT(b.id) FROM Bookmark b WHERE b.study.studyId = s.studyId)) * 5 " +
            " DESC"
    )
    Page<Study> findByPopularity(Pageable pageable);

    List<Study> findByMemberMemberIdAndIsDeletedFalse(Long memberId);

    Optional<Study> findByStudyIdAndIsDeletedFalse(Long studyId);
}

