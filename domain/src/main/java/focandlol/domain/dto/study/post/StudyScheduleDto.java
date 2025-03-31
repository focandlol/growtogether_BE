package focandlol.domain.dto.study.post;

import focandlol.domain.dto.study.schedule.MainScheduleDto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyScheduleDto {

    @NotNull(message = "날짜 리스트는 비워둘 수 없습니다.")
    @Size(min = 1, message = "메인일정을 반드시 1개 이상 입력해야 합니다.")
    private List<@NotBlank(message = "날짜는 빈 문자열일 수 없습니다.") String> dates;


    @NotBlank(message = "시간 필드는 비워둘 수 없습니다.")
    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "시간은 HH:mm 형식이어야 합니다.")
    private String time;

    @Min(value = 1, message = "총합은 1 이상이어야 합니다.")
    private int total;


    public static List<MainScheduleDto> formDto(StudyScheduleDto scheduleDto){
        return scheduleDto.dates.stream()
                .map(s -> new MainScheduleDto(s,scheduleDto.time,scheduleDto.total))
                .collect(Collectors.toList());
    }
}
