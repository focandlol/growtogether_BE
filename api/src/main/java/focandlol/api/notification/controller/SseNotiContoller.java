package focandlol.api.notification.controller;

import focandlol.api.notification.service.NotificationService;
import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.repository.MemberRepository;
import focandlol.domain.type.NotiType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Tag(name = "SSE Notification", description = "SSE(서버-전송 이벤트) 기반 실시간 알림 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseNotiContoller {

    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    @Operation(summary = "SSE 구독", description = "사용자가 SSE를 통해 실시간 알림을 구독합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 구독 성공",
                    content = @Content(schema = @Schema(implementation = SseEmitter.class))),
            @ApiResponse(responseCode = "500", description = "SSE 구독 중 서버 오류 발생")
    })
    @GetMapping(value = "/subscribe/{userId}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long userId, HttpServletResponse response){
        try {
            return notificationService.subscribe(userId);
        } catch (Exception e) {
            log.error("SSE 구독 중 오류 발생 : {} ", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Headers - Key : Accept Value: text/event-stream
     * @param id
     */
    @Operation(summary = "테스트 하고자 만든 메서드", description = "테스트 하고자 만든 메서드")
    @PostMapping("/send-data/{id}")
    public void sendData(@PathVariable Long id){
        String msg = "Noti test sendData " + id;

        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        notificationService.sendNotification(member,msg,"https://www.naver.com/", NotiType.BOOTCAMP);
    }


}
