package focandlol.chat.service;


import focandlol.common.auth.util.JwtUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtInterceptor implements ChannelInterceptor {

  private final JwtUtil jwtUtil;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    System.out.println("kkm151");

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = accessor.getFirstNativeHeader("Authorization");

      if (token == null || !token.startsWith("Bearer ")) {
        throw new IllegalArgumentException("JWT 토큰이 필요합니다.");
      }

      token = token.substring(7);

      jwtUtil.isTokenValid(token);
    }

    return message;
  }
}
