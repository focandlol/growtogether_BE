package focandlol.common.auth.util;

import static focandlol.common.exception.response.ErrorCode.EXPIRED_TOKEN;
import static focandlol.common.exception.response.ErrorCode.NOT_VALID_TOKEN;

import focandlol.common.exception.custom.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-token.expiration-time}")
    private long expirationTime;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 액세스 토큰을 발급하는 메서드
    public String generateAccessToken(String email, Long memberId, String nickName) {
        log.info("액세스 토큰이 발행되었습니다.");

        return "Bearer "+Jwts.builder()
                .claim("email", email) // 클레임에 email 추가
                .claim("memberId", memberId) // memberId 추가
                .claim("nickName",nickName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(this.getSigningKey())
                .compact();
    }

    // 응답 헤더에서 액세스 토큰을 반환하는 메서드
    public String getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 부분 제거하고 토큰만 추출
        }
        return null;
    }

    // 토큰에서 Claims(클레임) 파싱하는 메서드
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(NOT_VALID_TOKEN);
        }
    }

    // 토큰에서 유저 email을 가져오는 메서드
    public String getMemberEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }

    public String getNickNameFromToken (String token) {
        return getClaimsFromToken(token).get("nickName", String.class);
    }

    // 토큰에서 memberId를 가져오는 메서드
    public Long getMemberIdFromToken(String token) {
        return getClaimsFromToken(token).get("memberId", Long.class);
    }

    // Jwt 토큰의 유효기간을 확인하는 메서드
    public boolean isTokenValid(String token) {
        System.out.println("valid Method");
        try {
            Date expiration = getClaimsFromToken(token).getExpiration();
            log.info("토큰 만료 시간: {}", expiration);
            log.info("현재 시간: {}", new Date());
            return expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            throw new CustomException(EXPIRED_TOKEN);
        }
    }
}
