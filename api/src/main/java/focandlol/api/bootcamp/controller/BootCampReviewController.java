package focandlol.api.bootcamp.controller;

import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.bootcamp.BootCampReviewCreateDto;
import focandlol.domain.dto.bootcamp.BootCampReviewSearchRequest;
import focandlol.domain.dto.bootcamp.BootCampReviewUpdateDto;
import focandlol.api.bootcamp.service.BootCampReviewService;
import focandlol.domain.dto.bootcamp.BootCampReviewResponseDto.PageResponse;
import focandlol.domain.dto.bootcamp.BootCampReviewResponseDto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name ="Bootcamp Review",description = "부트캠프 리뷰 관련 API")
@RestController
@RequestMapping("/api/bootcamp")
@RequiredArgsConstructor
public class BootCampReviewController {

    private final BootCampReviewService bootCampReviewService;

    /**
     * 부트캠프 리뷰 등록
     */
    @Operation(summary = "부트캠프 리뷰 등록", description = "새로운 부트캠프 리뷰를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 등록 성공",
                    content = @Content(schema = @Schema(implementation = BootCampReviewCreateDto.class)))
    })
    @PostMapping
    public ResponseEntity<BootCampReviewCreateDto> createReview(@Valid @RequestPart("bootCampReview") BootCampReviewCreateDto request,
                                                                @RequestPart(value = "imageUrl",required = false) MultipartFile imageKey,
                                                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return ResponseEntity.ok(bootCampReviewService.createReview(request,imageKey,customUserDetails));
    }

    /**
     * 부트캠프 리뷰 수정
     */
    @Operation(summary = "부트캠프 리뷰 수정", description = "기존 부트캠프 리뷰를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공",
                    content = @Content(schema = @Schema(implementation = BootCampReviewUpdateDto.class)))
    })
    @PutMapping("/{bootCampId}")
    public ResponseEntity<BootCampReviewUpdateDto> updateReview(@PathVariable Long bootCampId, @Valid @RequestPart("bootCampReview") BootCampReviewUpdateDto request
            , @RequestPart(value = "imageUrl",required = false) MultipartFile imageKey,@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return ResponseEntity.ok(bootCampReviewService.updateReview(bootCampId ,request,imageKey,customUserDetails));
    }

    /**
     * 부트캠프 리뷰 삭제
     */
    @Operation(summary = "부트캠프 리뷰 삭제", description = "특정 부트캠프 리뷰를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 삭제 성공")
    })
    @DeleteMapping("/{bootCampId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long bootCampId) {

        bootCampReviewService.deleteReview(bootCampId);

        return ResponseEntity.ok("부트캠프 리뷰 삭제가 완료되었습니다.");
    }

    /**
     * 부트캠프 조회
     * 최신순 및 인기순 조회
     * 한 페이지당 9개
     */
    @Operation(summary = "부트캠프 리뷰 목록 조회", description = "최신순 및 인기순으로 부트캠프 리뷰 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse> getBootCampReviews(
            @RequestParam(defaultValue = "1") int page ,
            @RequestParam(defaultValue = "new") String sortType){

        return ResponseEntity.ok(bootCampReviewService.getBootCampReviews(page,sortType));
    }



    /**
     * 부트캠프 상세 조회
     */
    @Operation(summary = "부트캠프 리뷰 상세 조회", description = "특정 부트캠프 리뷰의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = Response.class)))
    })
    @GetMapping("/{bootCampId}")
    public ResponseEntity<Response> getBootCampDetail(@PathVariable Long bootCampId) {


        Response response = bootCampReviewService.getBootCampReviewDetail(bootCampId);

        return ResponseEntity.ok(response);
    }

    /**
     * 부트캠프 좋아요
     */
    @Operation(summary = "부트캠프 리뷰 좋아요 토글", description = "부트캠프 리뷰에 대한 좋아요 상태를 변경합니다.")
    @PostMapping("/{bootCampId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable Long bootCampId , @AuthenticationPrincipal CustomUserDetails customUserDetails){

        bootCampReviewService.toggleLike(bootCampId,customUserDetails);

        return ResponseEntity.ok("좋아요 상태가 변경되었습니다.");
    }

    /**
     * 부트캠프 검색 기능
     */
    @Operation(summary = "부트캠프 리뷰 검색", description = "키워드를 활용하여 부트캠프 리뷰를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<Response>> searchBootCamps(
            @ModelAttribute BootCampReviewSearchRequest request){

        List<Response> res = bootCampReviewService.searchBootCamps(request);

        return ResponseEntity.ok(res);
    }

    /**
     * 인기 게시글
     */
    @Operation(summary = "인기 부트캠프 리뷰 조회", description = "가중치 기반 알고리즘으로 인기 게시글을 조회합니다.")
    @GetMapping("/top")
    public List<Response> getTopReviews(
            @RequestParam(defaultValue = "WeightStrategy") String strategyType,
            @RequestParam(defaultValue = "5") int limit) {

        return bootCampReviewService.getTopBootCampReviews(strategyType, limit);
    }

    /**
     * 내가 좋아요한 게시글 모음
     */
/*    @Operation(summary = "내가 좋아요한 부트캠프 리뷰 조회", description = "사용자가 좋아요한 부트캠프 리뷰 목록을 조회합니다.")
    @GetMapping("/my/Likes")
    public ResponseEntity<BootCampReviewResponseDto.PageResponse> getLikedReviews(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "0") int page ,
            @RequestParam(defaultValue = "9") int size) {

        Pageable pageable = PageRequest.of(page,size);

        BootCampReviewResponseDto.PageResponse likedReviews = bootCampReviewService.getLikeReviews(customUserDetails, pageable);
        return ResponseEntity.ok(likedReviews);
    }*/

    @Operation(summary = "부트캠프 프로그램 과정 목록 조회", description = "프로그램 과정을 조회합니다.")
    @GetMapping("/programCourses")
    public List<String> getProgramCourse(){

        return bootCampReviewService.getProgramCourse();
    }
    @Operation(summary = "부트캠프 기술 스택 목록 조회", description = "부트캠프에서 사용하는 주요 기술 스택을 조회합니다.")
    @GetMapping("/skillName")
    public List<String> getSkillName(){

        return bootCampReviewService.getSkillName();
    }
}