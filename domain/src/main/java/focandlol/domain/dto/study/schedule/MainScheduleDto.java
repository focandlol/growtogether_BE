package focandlol.domain.dto.study.schedule;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class MainScheduleDto {
  LocalDateTime startTime;
  LocalDateTime endTime;
  Integer total;

  public MainScheduleDto(String date, String time, int total) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    this.startTime = LocalDateTime.parse(date + " " + time, formatter);
    this.endTime = this.startTime.plusMinutes(total);
    this.total = total;
  }
}
