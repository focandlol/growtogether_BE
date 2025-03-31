package focandlol.domain.dto.bootcamp;

import focandlol.domain.type.ProgramCourse;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootCampReviewSearchRequest {

    private String bootCampName;
    private String title;
    private ProgramCourse programCourse;
    private String skillName;

    @Min(0)
    private int page = 0;

    @Min(1)
    private int size = 9;

    private String sortType;
}
