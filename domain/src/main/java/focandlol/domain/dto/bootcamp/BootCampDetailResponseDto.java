package focandlol.domain.dto.bootcamp;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootCampDetailResponseDto {

    private Long id;
    private String nickname;
    private String title;
    private String content;
    private String bootCampName;
    private LocalDate bootcampStartDate;
    private LocalDate bootCampEndDate;
    private Integer learningLevel;
    private Integer assistantSatisfaction;
    private Integer progreamSatisfaction;
    private List<String> learningSkill;
    private Long viewCount;
    private Integer likeCount;
    private LocalDate createdAt;


}
