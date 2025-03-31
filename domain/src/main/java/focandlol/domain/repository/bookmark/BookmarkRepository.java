package focandlol.domain.repository.bookmark;

import focandlol.domain.entity.Bookmark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByMember_MemberIdAndStudy_StudyId(Long userId, Long studyId);
    int  countAllByMemberMemberId (Long memberId);

    Integer countAllByStudyStudyId(Long studyId);

    List<Bookmark> findByMember_MemberId(Long memberId);

}

