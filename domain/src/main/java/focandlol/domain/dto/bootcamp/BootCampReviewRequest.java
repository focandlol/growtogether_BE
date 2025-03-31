package focandlol.domain.dto.bootcamp;


import focandlol.domain.type.ProgramCourse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class BootCampReviewRequest {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;

    @NotBlank(message = "부트캠프 이름은 필수 입력값입니다.")
    private String bootCampName;

    @NotNull(message = "학습난이도는 필수 입력값입니다.")
    private Integer learningLevel;

    @NotNull(message = "취업 지원 만족도는 필수 입력값입니다.")
    private Integer assistantSatisfaction;

    @NotNull(message = "강의 만족도는 필수 입력값입니다.")
    private Integer programSatisfaction;

    @NotNull(message = "프로그램 과정은 필수 입력값 입니다.")
    private ProgramCourse programCourse;

    @NotNull(message = "부트캠프 시작 날짜는 필수 입력값입니다.")
    private LocalDate bootCampStartDate;

    @NotNull(message = "부트캠프 종료날짜는 필수 입력값입니다.")
    private LocalDate bootCampEndDate;
}