package focandlol.domain.dto.study.feedback;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequestDto {

  @NotEmpty
  @Valid
  private List<FeedbackCreateDto> feedbacks;

}
