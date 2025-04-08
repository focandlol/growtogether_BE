package focandlol.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import focandlol.chat.ChatManager;
import focandlol.chat.DeleteRedis;
import focandlol.chat.RedisChatMockDataGenerator;
import focandlol.consumer.chat.ChatProducer;
import focandlol.domain.dto.study.chat.ChatMessageDto;
import focandlol.domain.dto.study.chat.SliceMessageDto;
import focandlol.chat.service.ChatService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;
  private final ChatManager chatManager;
  private final SimpMessagingTemplate messagingTemplate;

  private final RedisChatMockDataGenerator redisChatMockDataGenerator;
  private final DeleteRedis deleteRedis;

  private final ChatProducer chatProducer;

  @MessageMapping("/study/{studyId}/send")
  public void sendMessage(@DestinationVariable String studyId, @Payload ChatMessageDto chatMessageDto) {
    chatMessageDto.setDate(LocalDateTime.now());
    chatProducer.sendMessage(chatMessageDto);
  }

  @PostMapping("/generate-chat")
  public ResponseEntity<String> generateChat() throws Exception {
    redisChatMockDataGenerator.generateMockChats();
    return ResponseEntity.ok("채팅 데이터 생성 완료");
  }

  @DeleteMapping("/delete-redis")
  public void deleteRedis() {
    deleteRedis.delete();
  }


  public void sendDirectMessage(String username, String studyId, ChatMessageDto message) {
    List<String> sessionIds = chatManager.getSessionIdsByUsername(username);
    for (String sessionId : sessionIds) {
      SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
      headerAccessor.setSessionId(sessionId);
      headerAccessor.setLeaveMutable(true);
      messagingTemplate.convertAndSendToUser(sessionId, "/queue/private/" + studyId, message,headerAccessor.getMessageHeaders());
    }
  }

  @MessageMapping("/study/{studyId}/participants")
  public void sendParticipants(@DestinationVariable String studyId) {
    Set<String> participants = chatManager.getParticipants(studyId);
    messagingTemplate.convertAndSend("/topic/study/" + studyId + "/participants", participants);
  }


  @GetMapping("/study/{studyId}/chat")
  public SliceMessageDto getChatMessages(@PathVariable Long studyId,
      @RequestParam(required = false) Integer lastIndex,
      @RequestParam(required = false) Integer size) {
    return chatService.getChatMessage(studyId, lastIndex, LocalDateTime.now(), size);
  }

}
