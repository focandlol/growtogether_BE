package focandlol.domain.repository;

import focandlol.domain.entity.BootCampComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BootCampCommentRepository extends JpaRepository<BootCampComment, Long> {

    boolean existsByBootCampCommentIdAndIsDeletedTrue(Long bootCampCommentId);

    @Query("SELECT DISTINCT c FROM BootCampComment c " +
            "LEFT JOIN FETCH c.childComments " +
            "LEFT JOIN FETCH c.member " +
            "WHERE c.bootCampReview.bootCampId =:bootCampId")
    List<BootCampComment> findCommentsWithChildrenByBootCampId(@Param("bootCampId") Long bootCampId);

    // 최초 요청 (부모 댓글만 가져오기)
    @EntityGraph(attributePaths = {"member"})
    @Query("""
        SELECT c FROM BootCampComment c 
        WHERE c.bootCampReview.bootCampId = :bootCampId 
        AND c.depth = 0 
        ORDER BY c.bootCampCommentId DESC
        """)
    Page<BootCampComment> findParentComments(
            @Param("bootCampId") Long bootCampId,
            Pageable pageable
    );

    // lastIdx 이후 데이터만 가져오기 (무한 스크롤 요청)
    @EntityGraph(attributePaths = {"member"})
    @Query("""
        SELECT c FROM BootCampComment c 
        WHERE c.bootCampReview.bootCampId = :bootCampId 
        AND c.bootCampCommentId < :lastIdx 
        AND c.depth = 0 
        ORDER BY c.bootCampCommentId DESC
        """)
    Page<BootCampComment> findParentCommentsWithLastIdx(
            @Param("bootCampId") Long bootCampId,
            @Param("lastIdx") Long lastIdx,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"member"})
    @Query("""
    SELECT c FROM BootCampComment c 
    WHERE c.parentComment.bootCampCommentId IN :parentCommentIds 
    ORDER BY c.bootCampCommentId DESC
""")
    List<BootCampComment> findChildCommentsByParentIds(@Param("parentCommentIds") List<Long> parentCommentIds);

}
