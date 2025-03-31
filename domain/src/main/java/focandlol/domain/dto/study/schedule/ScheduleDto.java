package focandlol.domain.dto.study.schedule;

import focandlol.domain.entity.schedule.ScheduleEntity;
import focandlol.domain.type.ScheduleType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDto {

  private Long scheduleId;

  private String title;

  private LocalDateTime start;

  private LocalDateTime end;

  private Integer totalTime;

  private ScheduleType scheduleType;

  private String author;

  public static ScheduleDto fromEntity(ScheduleEntity scheduleEntity) {
    return ScheduleDto.builder()
        .scheduleId(scheduleEntity.getId())
        .title(scheduleEntity.getTitle())
        .start(scheduleEntity.getStart())
        .end(scheduleEntity.getEnd())
        .totalTime(scheduleEntity.getTotalTime())
        .scheduleType(scheduleEntity.getType())
        .author(scheduleEntity.getStudyMember().getMember().getNickName())
        .build();
  }

}
