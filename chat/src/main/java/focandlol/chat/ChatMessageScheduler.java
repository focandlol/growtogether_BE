package focandlol.chat;

import static focandlol.domain.type.StudyMemberType.KICK;
import static focandlol.domain.type.StudyMemberType.LEADER;
import static focandlol.domain.type.StudyMemberType.NORMAL;
import static focandlol.domain.type.StudyStatus.PROGRESS;

import focandlol.domain.dto.study.chat.ChatMessageDto;
import focandlol.domain.entity.chat.ChatEntity;
import focandlol.domain.repository.chat.ChatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import focandlol.domain.entity.Study;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.repository.join.JoinRepository;
import focandlol.domain.repository.post.StudyRepository;
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
