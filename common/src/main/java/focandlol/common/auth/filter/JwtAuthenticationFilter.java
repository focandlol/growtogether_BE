package focandlol.common.auth.filter;

import static focandlol.common.exception.response.ErrorCode.NOT_VALID_TOKEN;

import focandlol.common.auth.CustomUserDetails;
import focandlol.common.auth.util.JwtUtil;
import focandlol.common.exception.custom.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String[] PUBLIC_URLS;
    private final String[] PUBLIC_GET_URLS;
    private final JwtUtil jwtUtil;
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher(); // 경로 패턴 매칭을 위한 AntPathMatcher

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청 URL이 퍼블릭 URL에 포함되는지 확인
        String requestUri = request.getRequestURI();
        boolean isPublicUrl = isPublicUrl(request.getMethod(), requestUri);

        if (isPublicUrl) {
            log.info("퍼블릭 URL - JWT 인증 생략: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        // 요청에서 JWT를 추출
        String jwtToken = jwtUtil.getTokenFromHeader(request);
        log.info("추출된 JWT: {}", jwtToken);

        // 토큰이 없거나 유효하지 않으면 인증 실패 처리
        if (jwtToken == null || !jwtUtil.isTokenValid(jwtToken)) {
            throw new CustomException(NOT_VALID_TOKEN);
        }

        // JWT 토큰에서 사용자 정보 추출
        String email = jwtUtil.getMemberEmailFromToken(jwtToken);
        Long memberId = jwtUtil.getMemberIdFromToken(jwtToken); // memberId 추가
        String nickName = jwtUtil.getNickNameFromToken(jwtToken);


        // 인증 객체 생성 (email과 memberId 함께 저장)
//        Map<String, Object> principalDetails = new HashMap<>();
//        principalDetails.put("email", email);
//        principalDetails.put("memberId", memberId);

        CustomUserDetails customerDetails = CustomUserDetails.builder().email(email).memberId(memberId).nickName(nickName).build();


        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customerDetails, null, null);

        // Security Context에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Security Context에 저장된 인증 정보: {}", SecurityContextHolder.getContext().getAuthentication());

        // 요청을 필터 체인에 전달
        filterChain.doFilter(request, response);
    }

    private boolean isPublicUrl(String httpMethod, String requestUri) {
        boolean check = Stream.of(PUBLIC_URLS).anyMatch(url -> antPathMatcher.match(url, requestUri));

        if (!check && "GET".equals(httpMethod)) {
            check = Stream.of(PUBLIC_GET_URLS).anyMatch(url -> antPathMatcher.match(url, requestUri));
        }

        return check;
    }
}
