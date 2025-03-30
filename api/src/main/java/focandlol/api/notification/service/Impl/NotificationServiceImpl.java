package focandlol.api.notification.service.Impl;

import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.dto.notification.NotificationDto;
import focandlol.api.notification.service.NotificationService;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.Notification;
import focandlol.domain.repository.EmitterRepository;
import focandlol.domain.repository.MemberRepository;
import focandlol.domain.repository.NotificationRepository;
import focandlol.domain.type.NotiType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분 타임 아웃
    private final MemberRepository memberRepository;

    /**
     * 클라이언트가 SSE(Server - Sent Events) 알림을 구독하기 위해 호출하는 메서드
     * @param userId
     * @return
     */
    @Override
    public SseEmitter subscribe(Long userId) {

        SseEmitter emitter = createEmitter(userId);

        Map<String,Object> connMessage = new HashMap<>();
        connMessage.put("type", "connection");
        connMessage.put("message", "Connected to SSE stream");
        connMessage.put("userId", userId);
        connMessage.put("timestamp", LocalDateTime.now().toString());


        // 클라이언트가 처음 구독할 때 기본 메시지 전송 (연결 안정성 확보)
        sendToClient(userId, connMessage);


        return emitter;
    }

    /**
     * 특정 사용사에게 알림을 전송 , 중요 알림 DB에 저장
     * @param member 알림을 받을 사용자
     * @param content 알림 내용
     * @param type 알림 유형
     */
    @Override
    public void sendNotification(MemberEntity member, String content,String url , NotiType type) {

        Notification notification = null;

        // 중요 알림만 DB에 저장
        if (importNoti(type)) {
            notification = Notification.builder()
                    .member(member)
                    .content(content)
                    .isCheck(false)
                    .type(type)
                    .url(url)
                    .build();

            notificationRepository.save(notification);
        }

        // SSE 실시간 알림 전송
        notifyUser(member.getMemberId(), new NotificationDto(
                notification != null ? notification.getNotiId() : null, // ✅ DB에 저장된 경우 ID 포함
                content,
                type,
                false,
                url
        ));
    }

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     * @param
     * @return 읽지 않는 알림 리스트
     */
    @Override
    public List<NotificationDto> getUnReadNotifiactions(String email) {

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Notification> notifications = notificationRepository.findByMemberAndIsCheckFalseOrderByCreatedAtDesc(member);

        return notifications.stream().map(notification -> new NotificationDto(
                notification.getNotiId(),
                notification.getContent(),
                notification.getType(),
                false,
                notification.getUrl()
        )).toList();
    }

    /**
     * 특정 알림 읽음 처리
     * @param notiId
     */
    @Override
    public void markNotification(Long notiId) {
        Notification notification = notificationRepository.findById(notiId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTI_NOT_FOUND));

        notification.markAsRead();
        notificationRepository.save(notification);

    }
    @Scheduled(fixedRate = 300_000)
    @Override
    public void sendHeartbeat() {
        List<Long> userIds = emitterRepository.getAllUserIds();

        for(Long userId : userIds){
            try{
                sendToClient(userId,Map.of(
                        "type", "heartbeat",
                        "message", "heartbeat로 인한 연결 유지",
                        "timestamp", LocalDateTime.now().toString()
                ));
            } catch(Exception e){
                log.error("하트비트 전송 실패: {} ",e.getMessage());
            }
        }
    }

    /**
     * 특정 사용자에게 SSE를 통한 알림 전송
     * @param userId 알림을 받을 사용자 id
     * @param notification 전송할 알림 데이터
     */
    private void notifyUser(Long userId, NotificationDto notification) {
        List<SseEmitter> emitters = emitterRepository.findByUserId(userId);
        List<SseEmitter> failEmitterList = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification).reconnectTime(3000));
            } catch (IOException e) {
                emitter.complete();
                failEmitterList.add(emitter);
                log.error("Failed to send SSE event: {} ", e.getMessage());
            }
        });

        failEmitterList.forEach(emitter -> emitterRepository.delete(userId, emitter));
    }

    /**
     * 클라이언트에게 데이터를 전송 (전송이 잘되는지 확인을 위한 테스트 메서드)
     *
     * @param id   - 데이터를 받을 사용자의 아이디.
     * @param data - 전송할 데이터.
     */
    private void sendToClient(Long id, Object data) {
        List<SseEmitter> emitters = emitterRepository.findByUserId(id);
        List<SseEmitter> failedEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(id))
                        .name("notification")
                        .data(data));

                log.info("✅ SSE 메시지 전송 완료: {}", data);
            } catch (IOException e) {

                emitter.completeWithError(e);
                failedEmitters.add(emitter);
                log.error("SSE 메시지 전송 실패: {} ", e.getMessage());
            }
        });

        failedEmitters.forEach(emitter -> emitterRepository.delete(id, emitter));
    }

    /**
     * 사용자 아이디를 기반으로 이벤트 Emitter를 생성
     *
     * @param id - 사용자 아이디.
     * @return SseEmitter - 생성된 이벤트 Emitter.
     */
    private SseEmitter createEmitter(Long id) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(id, emitter);

        // Emitter가 완료되거나 타임아웃되면 삭제
        emitter.onCompletion(() -> emitterRepository.deleteAllByUserId(id));
        emitter.onTimeout(() -> emitterRepository.deleteAllByUserId(id));

        return emitter;
    }

    private boolean importNoti(NotiType type) {
        return type.importNoti();
    }
}