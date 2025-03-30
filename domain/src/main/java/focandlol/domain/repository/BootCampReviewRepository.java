package focandlol.domain.repository;

import focandlol.domain.entity.BootCampReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BootCampReviewRepository extends JpaRepository<BootCampReview, Long> {


    @Query("SELECT b.bootCampId FROM BootCampReview b ORDER BY " +
            "CASE WHEN :sortType = 'hot' THEN b.likeCount END DESC, " +
            "CASE WHEN :sortType = 'new' THEN b.createdAt END DESC")
    Page<Long> findBootCampReviewIdsBySortType(@Param("sortType") String sortType, Pageable pageable);

    @Query("SELECT b FROM BootCampReview b " +
            "LEFT JOIN FETCH b.member " + // 단건 관계는 문제없음
            "LEFT JOIN FETCH b.bootCampSkills bs " +  // BootCampSkills 컬렉션 Fetch Join
            "LEFT JOIN FETCH bs.skill " + // Skill도 Fetch Join
            "WHERE b.bootCampId IN :ids")
    List<BootCampReview> findAllByIdsWithDetails(@Param("ids") List<Long> ids);

    @Query("SELECT b FROM BootCampReview b "
    + "LEFT JOIN FETCH b.bootCampSkills bs "
    + "LEFT JOIN FETCH bs.skill s" +
    " WHERE b.bootCampId = :bootCampId")
    Optional<BootCampReview> findByIdWithSkills(@Param("bootCampId")Long bootCampId);

    @Query("SELECT br.bootCampId, COUNT(c) FROM BootCampReview br LEFT JOIN br.comments c " +
            "WHERE br.bootCampId IN :bootCampIds GROUP BY br.bootCampId")
    List<Object[]> findCommentCountsByBootCampIds(@Param("bootCampIds") List<Long> bootCampIds);

}