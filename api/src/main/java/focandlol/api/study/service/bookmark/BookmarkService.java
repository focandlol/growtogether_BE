package focandlol.api.study.service.bookmark;

import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.entity.Bookmark;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.Study;
import focandlol.domain.repository.MemberRepository;
import focandlol.domain.repository.bookmark.BookmarkRepository;
import focandlol.domain.repository.post.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;

    public void setBookMark(Long memberId, Long studyId) {
        Optional<Bookmark> checkBookmark= bookmarkRepository.findByMember_MemberIdAndStudy_StudyId(memberId, studyId);
        if(checkBookmark.isPresent()){
            bookmarkRepository.delete(checkBookmark.get());
        }else{
            Study study = studyRepository.findById(studyId)
                    .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

            MemberEntity member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_INVALID_MEMBER));

            Bookmark bookmark = Bookmark.builder()
                    .member(member)
                    .study(study)
                    .build();

            bookmarkRepository.save(bookmark);
        }
    }
}

