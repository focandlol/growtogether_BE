package focandlol.common.exception.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {

  private ErrorCode errorCode; // 에러 코드 (ErrorCode Enum)

  private String description; // 상세 오류 메시지

  private HttpStatus status; // HTTP 상태 코드

  public ErrorResponse(ErrorCode errorCode){
    this.errorCode = errorCode;
    this.description = errorCode.getDescription();
    this.status = errorCode.getStatus();
  }

  public ErrorResponse(ErrorCode errorCode, String description){
    this.errorCode = errorCode;
    this.description = description;
    this.status = errorCode.getStatus();
  }
}
