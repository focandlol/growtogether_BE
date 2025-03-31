package focandlol.api.study.controller.bookmark;

import focandlol.api.study.service.bookmark.BookmarkService;
import focandlol.common.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study/bookmark")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{studyId}")
    public void addBookmark(@PathVariable long studyId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        bookmarkService.setBookMark(customUserDetails.getMemberId(),studyId);
    }
}

