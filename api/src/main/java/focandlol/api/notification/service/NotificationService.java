package focandlol.api.notification.service;

import focandlol.domain.dto.notification.NotificationDto;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.type.NotiType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NotificationService {

    //sse 구독
    SseEmitter subscribe(Long userId);

    //알림 전송
    void sendNotification(MemberEntity member,String content, String url, NotiType type);

    // 읽지 않는 알림 조회
    List<NotificationDto> getUnReadNotifiactions(String email);

    //알림 읽음 처리
    void markNotification(Long notiId);

    void sendHeartbeat();
}
