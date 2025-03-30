package focandlol.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisChatMockDataGenerator {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  public void generateMockChats() throws Exception {
    for (int studyId = 1; studyId <= 10000; studyId++) {
      String redisKey = "chat" + studyId;

      for (int i = 1; i <= 1000; i++) {
        ChatMessageDto chat = new ChatMessageDto(
            "user" + i,
            "스터디 " + studyId + " 메시지 " + i,
            LocalDateTime.now(),
            3L
        );

        String json = objectMapper.writeValueAsString(chat);
        redisTemplate.opsForList().leftPush(redisKey, json);
      }

      System.out.println("스터디 " + studyId + " 채팅 1000개 삽입 완료");
    }
  }

  @Getter
  static class ChatMessageDto {
    private String sender;

    private String message;

    private Long studyMemberId;

    private LocalDateTime date;

    public ChatMessageDto(String sender, String message, LocalDateTime date, Long studyMemberId) {
      this.sender = sender;
      this.message = message;
      this.date = date;
      this.studyMemberId = studyMemberId;
    }
  }
}
