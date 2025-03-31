package focandlol.domain.repository;


import focandlol.domain.entity.BootCampReview;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.ReviewLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByBootCampReviewAndMember(BootCampReview bootCampReview, MemberEntity member);

    void deleteByBootCampReviewAndMember(BootCampReview bootCampReview, MemberEntity member);

    Page<ReviewLike> findByMember(MemberEntity member, Pageable pageable);
    int  countAllByMemberMemberId (Long memberId);

    List<ReviewLike> findByMemberMemberId(Long memberId);

}
