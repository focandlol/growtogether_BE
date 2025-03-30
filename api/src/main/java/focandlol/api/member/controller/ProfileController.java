package focandlol.api.member.controller;

import focandlol.api.member.service.MemberService;
import focandlol.api.member.service.S3Service;
import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/member/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final S3Service s3Service;
    private final MemberService memberService;

    //  이미지 업로드
    @PostMapping("/upload")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        String fileUrl = s3Service.uploadFile(file);
        return ResponseEntity.ok(Map.of("message", "이미지 업로드 성공", "imageUrl", fileUrl));
    }
    // 프로필 이미지 URL 조회 API
    @GetMapping("/image/{fileKey}")
    public ResponseEntity<?> getProfileImageUrl(@PathVariable String fileKey) {
        String fileUrl = s3Service.getFileUrl(fileKey);
        return ResponseEntity.ok(Map.of("profileImageUrl", fileUrl));
    }

    // 사용자 ID로 프로필 이미지 조회
    @GetMapping("/image/{memberId}")
    public ResponseEntity<?> getProfileImageByMemberId(@PathVariable Long memberId) {
        try {
            // 프로필 이미지 조회
            String fileUrl = memberService.getProfileImageUrl(memberId);
            return ResponseEntity.ok(Map.of("profileImageUrl", fileUrl));

        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(Map.of("errorCode", e.getErrorCode().name(), "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorCode", ErrorCode.FILE_STORAGE_ERROR.name(),
                            "message", "프로필 이미지 조회 중 예상치 못한 오류가 발생했습니다."));
        }
    }
    // 프로필 이미지 업데이트
    @PutMapping("image/update")
    public ResponseEntity<?> updateProfileImage(
            @RequestParam("memberId") Long memberId,
            @RequestPart("profileImage") MultipartFile profileImage) {

        try {
            // 프로필 이미지 업데이트 후, S3 URL 반환
            String newImageUrl = memberService.updateProfileImage(memberId, profileImage);

            return ResponseEntity.ok(Map.of(
                    "message", "프로필 이미지가 업데이트되었습니다.",
                    "imageUrl", newImageUrl
            ));

        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(Map.of("errorCode", e.getErrorCode().name(), "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorCode", ErrorCode.FILE_UPLOAD_FAILED.name(),
                            "message", "프로필 이미지 업데이트 중 예상치 못한 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("image/delete")
    public ResponseEntity<?> deleteProfileImage(@RequestParam("memberId") Long memberId) {
        try {
            // 프로필 이미지 삭제 요청
            memberService.deleteProfileImage(memberId);
            return ResponseEntity.ok(Map.of("message", "프로필 이미지가 삭제되었습니다."));

        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(Map.of("errorCode", e.getErrorCode().name(), "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorCode", ErrorCode.FILE_DELETE_FAILED.name(),
                            "message", "프로필 이미지 삭제 중 예상치 못한 오류가 발생했습니다."));
        }
    }


}