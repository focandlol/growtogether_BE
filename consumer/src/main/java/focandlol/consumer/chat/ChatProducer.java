package focandlol.consumer.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import focandlol.domain.dto.study.chat.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatProducer {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public void sendMessage(ChatMessageDto message) {
    try {
      String json = objectMapper.writeValueAsString(message);
      kafkaTemplate.send("chat-messages", String.valueOf(message.getStudyId()), json);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
