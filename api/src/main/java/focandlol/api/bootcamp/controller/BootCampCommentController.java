package focandlol.api.bootcamp.controller;

import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.bootcamp.CommentRequest;
import focandlol.domain.dto.bootcamp.CommentResponseDto;
import focandlol.domain.dto.bootcamp.CommentUpdateRequest;
import focandlol.api.bootcamp.service.BootCampCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bootcamp Comment", description = "부트캠프 리뷰 댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bootcamp/comments")
public class BootCampCommentController {

    private final BootCampCommentService bootCampCommentService;

    @Operation(summary = "댓글 작성", description = "부트캠프 리뷰에 새로운 댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공")
    })
    @PostMapping
    public ResponseEntity<String> addComment(@RequestBody CommentRequest request , @AuthenticationPrincipal CustomUserDetails customUserDetails){

        bootCampCommentService.addComment(request,customUserDetails);


        return ResponseEntity.ok("댓글 작성이 완료되었습니다.");
    }

    @Operation(summary = "댓글 수정", description = "작성한 댓글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    })
    @PutMapping("/{bootCampCommentId}")
    public ResponseEntity<String> updateComment(@PathVariable Long bootCampCommentId, @RequestBody CommentUpdateRequest request,@AuthenticationPrincipal CustomUserDetails customUserDetails){

        bootCampCommentService.updateComment(bootCampCommentId,request.getContent(), customUserDetails);

        return ResponseEntity.ok("댓글 수정이 완료되었습니다.");
    }

    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공")
    })
    @DeleteMapping("/{bootCampCommentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long bootCampCommentId,@AuthenticationPrincipal CustomUserDetails customUserDetails){

        bootCampCommentService.deleteComment(bootCampCommentId,customUserDetails);

        return ResponseEntity.ok("댓글 삭제가 완료되었습니다.");
    }

    @Operation(summary = "부트캠프 리뷰 댓글 조회", description = "특정 부트캠프 리뷰에 대한 댓글 목록을 조회합니다. 한 페이지당 10개의 댓글을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommentResponseDto.class)))
    })

    @GetMapping("/{bootCampId}")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long bootCampId ,
            @RequestParam(defaultValue = "0") Long lastIdx
            , @RequestParam(defaultValue = "9") Long size){

        List<CommentResponseDto> comments = bootCampCommentService.getComments(bootCampId,lastIdx,size);

        return ResponseEntity.ok(comments);

    }

}
