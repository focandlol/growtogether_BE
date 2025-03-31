package focandlol.chat;

import focandlol.common.auth.util.JwtUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class ChatManager {

  private final RedisTemplate<String, String> redisTemplate;
  private final SimpMessagingTemplate messagingTemplate;
  private final JwtUtil jwtUtil;

  private final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();
  private final Map<String, List<String>> usernameToSessions = new ConcurrentHashMap<>();

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String token = headerAccessor.getFirstNativeHeader("Authorization");
    String studyId = headerAccessor.getFirstNativeHeader("studyId");

    token = token.substring(7);
    jwtUtil.isTokenValid(token);
    String username = jwtUtil.getNickNameFromToken(token);

    redisTemplate.opsForSet().add("chatRoom" + studyId, username);

    String sessionId = headerAccessor.getSessionId();
    registerSession(sessionId, username);

    headerAccessor.getSessionAttributes().put("username", username);
    headerAccessor.getSessionAttributes().put("studyId", studyId);

    sendParticipants(studyId);
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = event.getSessionId();

    String username = (String) headerAccessor.getSessionAttributes().get("username");
    String studyId = (String) headerAccessor.getSessionAttributes().get("studyId");

    redisTemplate.opsForSet().remove("chatRoom" + studyId, username);

    removeSession(sessionId);

    sendParticipants(studyId);
  }

  public void sendDirectMessage(String username, String studyId, String message) {
    List<String> sessionIds = getSessionIdsByUsername(username);
    for (String sessionId : sessionIds) {
      messagingTemplate.convertAndSendToUser(sessionId, "/queue/private/" + studyId, message);
    }
  }

  private void sendEntryMessage(String studyId, String username) {
    String message = username + "님이 채팅방에 입장하였습니다.";
    messagingTemplate.convertAndSend("/topic/study/" + studyId + "/messages", message);
  }

  public Set<String> getParticipants(String studyId) {
    String participantsKey = "chatRoom" + studyId;
    return redisTemplate.opsForSet().members(participantsKey);
  }

  private void sendParticipants(String studyId) {
    String participantsKey = "chatRoom" + studyId;
    Set<String> participants = redisTemplate.opsForSet().members(participantsKey);
    messagingTemplate.convertAndSend("/topic/study/" + studyId + "/participants", participants);
  }

  public void registerSession(String sessionId, String username) {
    sessionToUsername.put(sessionId, username);
    usernameToSessions.putIfAbsent(username, new ArrayList<>());
    usernameToSessions.get(username).add(sessionId);
  }

  public void removeSession(String sessionId) {
    String username = sessionToUsername.remove(sessionId);
    if (username != null) {
      List<String> sessions = usernameToSessions.get(username);
      if (sessions != null) {
        sessions.remove(sessionId);
        if (sessions.isEmpty()) {
          usernameToSessions.remove(username);
        }
      }
    }
  }

  public List<String> getSessionIdsByUsername(String username) {
    return usernameToSessions.getOrDefault(username, Collections.emptyList());
  }
}


