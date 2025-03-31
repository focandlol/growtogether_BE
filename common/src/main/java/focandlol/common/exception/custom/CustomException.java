package focandlol.common.exception.custom;

import focandlol.common.exception.response.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode; // 예외의 종류를 나타내는 ErrorCode Enum

  private final String description; // 예외 상세 메시지

  private final HttpStatus status; // HTTP 상태 코드

  public CustomException(ErrorCode errorCode){
    super(errorCode.getDescription());
    this.errorCode = errorCode;
    this.description = errorCode.getDescription();
    this.status = errorCode.getStatus();
  }

}
