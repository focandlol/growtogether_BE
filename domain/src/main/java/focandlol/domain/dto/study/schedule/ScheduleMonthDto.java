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
public class ScheduleMonthDto {
  private List<ScheduleGroup> schedules;

  public static ScheduleMonthDto from(Map<LocalDate, List<ScheduleDto>> groupedSchedules) {
    List<ScheduleGroup> scheduleGroups = groupedSchedules.entrySet().stream()
        .map(entry -> new ScheduleGroup(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    return new ScheduleMonthDto(scheduleGroups);
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ScheduleGroup {
    private LocalDate date;
    private List<ScheduleDto> schedule;
  }
}
