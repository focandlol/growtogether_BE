package focandlol.api.member.controller;

import focandlol.api.bootcamp.service.BootCampReviewService;
import focandlol.api.study.service.post.StudyService;
import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.bootcamp.BootCampReviewResponseDto;
import focandlol.domain.dto.member.MyPageInfoDto;
import focandlol.domain.dto.member.MyPageLikesDto;
import focandlol.domain.dto.member.MyPageUpdateDto;
import focandlol.api.member.service.MemberService;
import focandlol.api.member.service.MyPageService;
import focandlol.domain.dto.study.post.StudyDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {


    private final BootCampReviewService bootCampReviewService;
    private final MyPageService myPageService;
    private final StudyService studyService;
    private final MemberService memberService;

    /**
     * 부트캠프 좋아요
     */
    @Operation(summary = "내가 좋아요한 부트캠프 리뷰 조회", description = "사용자가 좋아요한 부트캠프 리뷰 목록을 조회합니다.")
    @GetMapping("/myLikes")
    public ResponseEntity<BootCampReviewResponseDto.PageResponse> getLikedReviews(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        Pageable pageable = PageRequest.of(page, size);

        BootCampReviewResponseDto.PageResponse likedReviews = bootCampReviewService.getLikeReviews(customUserDetails, pageable);
        return ResponseEntity.ok(likedReviews);
    }

    @Operation(summary = "마이페이지 프로필 및 좋아요한 게시글 조회",
            description = "닉네임, 프로필 이미지, 포인트, 깃허브 URL, 기술 스택 및 좋아요한 게시글(찜한 부트캠프 후기게시글 + 북마크한 스터디 게시글)을 조회합니다.")
    @GetMapping("/info")
    public ResponseEntity<MyPageInfoDto> getMyPageInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        MyPageInfoDto myPageInfo = myPageService.getMyPageInfo(customUserDetails.getMemberId());
        return ResponseEntity.ok(myPageInfo);
    }

    @Operation(summary = "내가 참여 중인 스터디 리스트 조회",
            description = "참여 중인 스터디 개수와 좋아요한 게시글(스터디 북마크 + 부트캠프 리뷰 좋아요) 개수를 조회합니다.")
    @GetMapping("/studies")
    public ResponseEntity<List<StudyDTO>> getStudies(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<StudyDTO> studies = studyService.getMyStudies(customUserDetails.getMemberId());
        return ResponseEntity.ok(studies);
    }

    //마이 페이지 내가 참여중인 스터디 갯수 + 좋아요한 부트캠프 리뷰 게시판 갯수
    @Operation(summary = "내가 참여 중인 스터디 개수 및 좋아요한 게시글 개수 조회",
            description = "참여 중인 스터디 개수와 좋아요한 게시글(스터디 북마크 + 부트캠프 리뷰 좋아요) 개수를 조회합니다.")
    @GetMapping("/liked-posts")
    public ResponseEntity<List<MyPageLikesDto>> getMyLikedPosts(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<MyPageLikesDto> bookmarks = myPageService.getMyLikedPosts(customUserDetails.getMemberId());
        return ResponseEntity.ok(bookmarks);
    }

    @Operation(summary = "내가 참여 중인 스터디 리스트 조회",
            description = "참여 중인 스터디 개수와 좋아요한 게시글(스터디 북마크 + 부트캠프 리뷰 좋아요) 개수를 조회합니다.")
    @PutMapping("/update-my-Info")
    public ResponseEntity<MyPageUpdateDto> updateMyPageInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                            @RequestBody MyPageUpdateDto myPageUpdateDto) {
        MyPageUpdateDto updateMyPageInfo = myPageService.updateMyPageInfo(customUserDetails.getMemberId(), myPageUpdateDto);
        return ResponseEntity.ok(updateMyPageInfo);
    }

}
