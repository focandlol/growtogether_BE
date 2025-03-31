package focandlol.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import focandlol.domain.dto.study.chat.ChatMessageDto;
import focandlol.domain.dto.study.chat.SliceMessageDto;
import focandlol.domain.entity.chat.ChatEntity;
import focandlol.domain.repository.chat.ChatRepository;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.repository.join.JoinRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;
  private final ChatRepository chatRepository;
  private final JoinRepository joinRepository;

  public SliceMessageDto getChatMessage(Long studyId, Integer lastIndex, LocalDateTime date, int size){

    String key = "chat" + studyId;

    List<ChatMessageDto> chatMessages = new ArrayList<>();
    int start, end;

    if (lastIndex == null) {
      start = 0;
      end = size - 1;
    }
    else if (lastIndex > 0) {
      start = lastIndex;
      end = start + size - 1;
    }
    else {
      return fetchFromDB(studyId, lastIndex, size, date, chatMessages);
    }

    List<Object> range = redisTemplate.opsForList().range(key, start, end);
    if (range != null && !range.isEmpty()) {
      List<ChatMessageDto> collect = range.stream()
          .map(json -> jsonToChatMessageDto(json))
          .collect(Collectors.toList());

      List<Long> ids = collect.stream()
          .map(a -> a.getStudyMemberId())
          .distinct()
          .collect(Collectors.toList());

      Map<Long, StudyMemberEntity> smMap = joinRepository.findAllWithMembersInIds(ids)
          .stream().collect(Collectors.toMap(sm -> sm.getId(), Function.identity()));

      collect.forEach(dto -> {
        StudyMemberEntity sender = smMap.get(dto.getStudyMemberId());
        if(sender != null){
          dto.setSender(sender.getMember().getNickName());
        }
      });

      chatMessages.addAll(collect);

      if (chatMessages.size() == size) {
        return SliceMessageDto.builder()
            .lastIndex(end + 1)
            .lastDate(collect.get(collect.size()-1).getDate())
            .chatMessages(chatMessages)
            .build();
      }
    }

    return fetchFromDB(studyId, null, size - chatMessages.size(), date , chatMessages);

  }

  private SliceMessageDto fetchFromDB(Long studyId, Integer lastIndex, int size, LocalDateTime date, List<ChatMessageDto> redisMessages) {
    LocalDateTime lastDate = LocalDateTime.now();
    if (!redisMessages.isEmpty()) {
      lastDate = redisMessages.get(redisMessages.size() - 1).getDate();
    } else {
      lastDate = date;
    }

    List<ChatEntity> dbMessages = new ArrayList<>();

    if(lastIndex == null || lastIndex > 0) {
      Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "date"));
      dbMessages = chatRepository
          .findByStudy_StudyIdAndDateBefore(studyId, lastDate, pageable);
    }else if(lastIndex < 0){
      Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
      dbMessages = chatRepository.findByStudy_StudyIdAndIdLessThanOrderByIdDesc(studyId,
          (long) -lastIndex, pageable);
    }

    List<ChatMessageDto> chatMessageDtos = dbMessages.stream()
        .map(chat -> ChatMessageDto.builder()
            .studyId(chat.getStudy().getStudyId())
            .message(chat.getMessage())
            .sender(chat.getSender().getMember().getNickName())
            .studyMemberId(chat.getSender().getId())
            .imageUrl(chat.getImageUrl())
            .date(chat.getDate()).build())
        .collect(Collectors.toList());

    redisMessages.addAll(chatMessageDtos);

    long lastPk = dbMessages.isEmpty() ? -1 : -dbMessages.get(dbMessages.size() - 1).getId();

    return SliceMessageDto.builder()
        .lastIndex((int) lastPk)
        .chatMessages(redisMessages)
        .lastDate(redisMessages.size() > 0 ? redisMessages.get(redisMessages.size()-1).getDate() : null)
        .build();
  }

  private ChatMessageDto jsonToChatMessageDto(Object json) {
    try {
      return objectMapper.readValue(json.toString(), ChatMessageDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed", e);
    }
  }

}
