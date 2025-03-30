package focandlol.chat;

import focandlol.chat.dto.ChatMessageDto;
import focandlol.chat.entity.ChatEntity;
import focandlol.chat.repository.ChatRepository;
import com.campfiredev.growtogether.study.entity.Study;
import com.campfiredev.growtogether.study.entity.join.StudyMemberEntity;
import com.campfiredev.growtogether.study.repository.join.JoinRepository;
import com.campfiredev.growtogether.study.repository.post.StudyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.campfiredev.growtogether.study.entity.StudyStatus.PROGRESS;
import static com.campfiredev.growtogether.study.type.StudyMemberType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageScheduler {

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final ChatRepository chatMessageRepository;
  private final StudyRepository studyRepository;
  private final JoinRepository joinRepository;

  @Scheduled(cron = "0 0 0 * * *")
  //@Scheduled(fixedRate = 1000 * 60 * 2)
  public void persistOldMessages() {

    List<Study> studies = studyRepository.findByStudyStatus(PROGRESS);

    log.info("chat scheduler");

    for (Study study : studies) {
      String redisKey = "chat" + study.getStudyId();

      List<String> oldMessages = redisTemplate.opsForList().range(redisKey, 3, -1);

      log.info("oldMessages size: " + oldMessages.size());
      if (oldMessages.size() == 0 || oldMessages == null) {
        continue;
      }


      List<ChatMessageDto> messagesToSave = oldMessages.stream().map(msg -> {
        try {
          return objectMapper.readValue(msg, ChatMessageDto.class);
        } catch (Exception e) {
          log.error("Failed: {}", msg, e);
          return null;
        }
      }).filter(msg -> msg != null).collect(Collectors.toList());

      messagesToSave.sort(Comparator.comparing(ChatMessageDto::getDate));

      List<StudyMemberEntity> findStudyMembers = joinRepository.findByStudyWithMembersInStatus(
          study.getStudyId(), List.of(NORMAL, LEADER, KICK));

      Map<Long, StudyMemberEntity> studyMemberMap = findStudyMembers.stream()
          .collect(Collectors.toMap(sm -> sm.getId(), Function.identity()));

      List<ChatEntity> collect = messagesToSave.stream()
          .map(a -> ChatEntity.builder()
              .study(study)
              .message(a.getMessage())
              .date(a.getDate())
              .sender(studyMemberMap.get(a.getStudyMemberId()))
              .imageUrl(a.getImageUrl()).build())
          .collect(Collectors.toList());

      chatMessageRepository.saveAll(collect);

      log.info("after save");

      deleteInRedis(redisKey, oldMessages.size());
    }
  }

  public void deleteInRedis(String redisKey, int oldMessageCount) {
    DefaultRedisScript<Void> script = new DefaultRedisScript<>();
    script.setScriptText(
        "local key = KEYS[1] " +
            "local deleteCount = tonumber(ARGV[1]) " +
            "local size = redis.call('LLEN', key) " +
            "if size > deleteCount then " +
            "  redis.call('LTRIM', key, 0, size - deleteCount - 1) " +
            "end"
    );
    script.setResultType(Void.class);

    redisTemplate.execute(script, List.of(redisKey), String.valueOf(oldMessageCount));
  }

}
