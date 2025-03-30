package focandlol.common.exception.test;

import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * 테스트용
 */
@Service
public class TestService {

  public void throwCustomException(String a){
    if(a.equals("throw")){
      throw new CustomException(ErrorCode.ALREADY_JOINED_STUDY);
    }
  }

  public void throwException(String a) throws IllegalAccessException {
    if(a.equals("throw")){
      throw new IllegalAccessException();
    }
  }

}
