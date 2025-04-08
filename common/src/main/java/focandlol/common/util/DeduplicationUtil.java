package focandlol.common.util;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeduplicationUtil {

  private final RedisTemplate<String, String> redisTemplate;
  private static final String PREFIX = "kafka:dedup:";

  public boolean isDuplicate(String key) {
    String fullKey = PREFIX + key;
    Boolean success = redisTemplate.opsForValue().setIfAbsent(fullKey, "1", Duration.ofMinutes(10));
    return success == Boolean.FALSE;
  }
}
