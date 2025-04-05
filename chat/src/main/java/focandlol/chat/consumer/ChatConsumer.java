package focandlol.chat.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import focandlol.domain.dto.study.chat.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatConsumer {
  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;
  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  @KafkaListener(topics = "chat-messages", groupId = "${spring.kafka.consumer.group-id}")
  public void consume(String payload) {
    log.info("Consumer groupId: {}", groupId);
    try {
      ChatMessageDto message = objectMapper.readValue(payload, ChatMessageDto.class);
      messagingTemplate.convertAndSend("/topic/study/" + message.getStudyId(), message);
    } catch (Exception e) {
      log.info("consumer exception:");
      throw new RuntimeException(e);
    }
  }
}
