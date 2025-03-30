package focandlol.domain.dto.study.attendance;

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
public class AttendanceDto {
  private Long scheduleId;
  private LocalDateTime date;
  private List<String> attendees;
}
