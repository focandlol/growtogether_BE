package focandlol.domain.dto.study.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleUpdateDto {

  @NotBlank
  private String title;

  @NotNull
  private LocalDate startDate;

  @NotNull
  private LocalTime startTime;

  @NotNull
  private Integer totalTime;

}
