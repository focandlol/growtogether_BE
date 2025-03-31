package focandlol.domain.dto.study.chat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SliceMessageDto {

  private Integer lastIndex;

  private LocalDateTime lastDate;

  private List<ChatMessageDto> chatMessages;
}
