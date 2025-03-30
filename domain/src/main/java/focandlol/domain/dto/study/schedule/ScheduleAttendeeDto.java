package focandlol.domain.dto.study.schedule;

import focandlol.domain.entity.schedule.ScheduleEntity;
import focandlol.domain.type.ScheduleType;
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
public class ScheduleAttendeeDto {

  private Long scheduleId;
  private String title;
  private LocalDateTime start;
  private LocalDateTime end;
  private Integer totalTime;
  private ScheduleType type;
  private String author;
  private List<String> attendees;

  public static ScheduleAttendeeDto create(ScheduleEntity schedule, String nickName, List<String> attendedNicknames) {
    return ScheduleAttendeeDto.builder()
        .scheduleId(schedule.getId())
        .title(schedule.getTitle())
        .start(schedule.getStart())
        .end(schedule.getEnd())
        .totalTime(schedule.getTotalTime())
        .type(schedule.getType())
        .author(nickName)
        .attendees(attendedNicknames)
        .build();
  }
}
