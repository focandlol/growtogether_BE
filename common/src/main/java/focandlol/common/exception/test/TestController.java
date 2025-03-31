package focandlol.common.exception.test;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트용
 */
@RestController
@RequiredArgsConstructor
public class TestController {

  private final TestService testService;

  @GetMapping("/custom-exception")
  public ResponseEntity<String> throwCustomException() {
    testService.throwCustomException("throw");
    return ResponseEntity.ok("ok");
  }

  @GetMapping("/exception")
  public ResponseEntity<String> throwException() throws IllegalAccessException {
    testService.throwException("throw");
    return ResponseEntity.ok("ok");
  }

}
