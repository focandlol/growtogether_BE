package focandlol.domain.dto.study.feedback;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackCreateDto {

  @NotNull
  private Long studyMemberId;

  @Size(max = 255)
  private String content;

  @DecimalMin(value = "0.0", inclusive = true)
  @DecimalMax(value = "5.0", inclusive = true)
  private Double score;

}
