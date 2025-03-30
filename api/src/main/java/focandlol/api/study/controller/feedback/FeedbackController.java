package focandlol.api.study.controller.feedback;

import focandlol.api.study.service.feedback.FeedbackService;
import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.study.feedback.FeedbackRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/study")
public class FeedbackController {

  private final FeedbackService feedbackService;

  @PostMapping("/{studyId}/feedback")
  public void feedback(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long studyId, @RequestBody @Valid FeedbackRequestDto feedbackRequestDto){
    feedbackService.feedback(customUserDetails.getMemberId(), studyId,feedbackRequestDto.getFeedbacks());
  }

}
