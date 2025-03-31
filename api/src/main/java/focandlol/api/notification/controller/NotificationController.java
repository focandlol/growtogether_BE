package focandlol.api.notification.controller;

import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.notification.NotificationDto;
import focandlol.api.notification.service.NotificationService;
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
@Tag(name = "Notification", description = "알림 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/noti")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 사용자의 읽지 않는 알림 목록 조회
     *
     * @return 읽지 않는 알림 리스트
     */
    @Operation(summary = "읽지 않은 알림 조회", description = "사용자의 읽지 않은 알림 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "읽지 않은 알림 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationDto.class)))
    })
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        List<NotificationDto> notificationDtos = notificationService.getUnReadNotifiactions(customUserDetails.getEmail());

        return ResponseEntity.ok(notificationDtos);
    }

    /**
     * 특정 알림 읽음 처리
     * @param notiId
     * @return
     */
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
    })
    @PutMapping("{notiId}/read")
    public ResponseEntity<String> markNotiRead(@PathVariable Long notiId){
        notificationService.markNotification(notiId);

        return ResponseEntity.ok("해당 알림을 읽으셨습니다.");
    }

}
