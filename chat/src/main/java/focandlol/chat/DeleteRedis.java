package focandlol.chat;

import jakarta.ejb.Local;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteRedis {

  private final RedisTemplate<String, Object> redisTemplate;

  public void delete(){
    for(int i=1; i<=10000; i++){
      String redisKey = "chat" + i;

      redisTemplate.delete(redisKey);

      log.info(redisKey + " 삭제");
    }
  }

}
