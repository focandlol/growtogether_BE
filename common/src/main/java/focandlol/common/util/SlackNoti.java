package focandlol.common.util;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlackNoti {
  private final RestTemplate restTemplate;

  @Value("${slack.webhook.url}")
  private String url;

  @Async
  public void send(String message){
    Map<String, String> payload = new HashMap<>();
    payload.put("text", message);
    log.info("Sending Slack notification to");
    try {
      restTemplate.postForEntity(url, payload, String.class);
    } catch (Exception e) {
      log.error("Slack 알림 전송 실패", e);
    }
  }
}
