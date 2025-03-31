package focandlol.domain.dto.study.chat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

  private Long studyId;

  private String sender;

  private String message;

  private Long studyMemberId;

  private String imageUrl;

  private LocalDateTime date;

  private List<String> to;

}
