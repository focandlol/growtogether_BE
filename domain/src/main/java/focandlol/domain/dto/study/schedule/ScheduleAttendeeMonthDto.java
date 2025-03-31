package focandlol.domain.dto.study.schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleAttendeeMonthDto {
  private List<ScheduleAttendeeGroup> schedules;

  public static ScheduleAttendeeMonthDto from(
      Map<LocalDate, List<ScheduleAttendeeDto>> groupedSchedules) {
    List<ScheduleAttendeeGroup> scheduleGroups = groupedSchedules.entrySet().stream()
        .map(entry -> new ScheduleAttendeeGroup(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    return new ScheduleAttendeeMonthDto(scheduleGroups);
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ScheduleAttendeeGroup {
    private LocalDate date;
    private List<ScheduleAttendeeDto> schedule;
  }
}

