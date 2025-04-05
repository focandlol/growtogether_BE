package focandlol.chat.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import focandlol.common.util.SlackNoti;
import focandlol.domain.dto.study.chat.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatDltListener {

  private final ObjectMapper objectMapper;
  private final SlackNoti slackNoti;

  @KafkaListener(
      topics = "chat-messages.dlt",
      groupId = "chat-dlt-logger",
      id = "chat-dlt-listener",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handleDeadLetter(String payload, Acknowledgment ack) {
    try {
      ChatMessageDto message = objectMapper.readValue(payload, ChatMessageDto.class);
      slackNoti.send("DLT 메시지 발생\n" +
          "studyId: " + message.getStudyId() + "\n" +
          "content: " + message.getMessage() + "\n" +
          "date: " + message.getDate());

      log.info("메시지 처리 성공: {}", message.getStudyId());
    } catch (Exception e) {
      log.error("DLT 메시지 역직렬화 실패: {}", payload, e);
      return;
    }
    ack.acknowledge();
  }
}

