package focandlol.domain.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface EmitterRepository {

    /**
     * sse 구독 요청이 오면 save를 통해서 저장
     */
    void save(Long userId, SseEmitter emitter);

    /**
     * 연결이 끊이면 delete를 통해서 제거
     */
    void delete(Long userId, SseEmitter emitter);

    /**
     *  특정 사용자에게 알림을 보낼 때 findByUserId를 통해서 Emitter 조회
     */
    List<SseEmitter> findByUserId(Long userId);

    /**
     * 특정 사용자 관련하여 모든 연결 삭제
     */
    void deleteAllByUserId(Long userId);

    //현재 SSE에 연결된 모든 사용자 ID 조회
    List<Long> getAllUserIds();
}
