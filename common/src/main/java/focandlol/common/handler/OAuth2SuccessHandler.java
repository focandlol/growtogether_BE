package focandlol.common.handler;

import focandlol.common.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        Long memberId = oAuth2User.getAttribute("memberId");


        // JWT 발급
        String token = jwtUtil.generateAccessToken(email,memberId,"ac");
        response.setHeader("Authorization", "Bearer " + token);
        System.out.println("토큰이 ㅂㄹ급ㄷ");

        // 인증 객체 생성 & SecurityContext에 등록
        UsernamePasswordAuthenticationToken jwtAuth = new UsernamePasswordAuthenticationToken(email, token, authentication.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(jwtAuth);
        System.out.println("da");

        // 로그인 성공 후 리다이렉트 (예: 메인 페이지)
        getRedirectStrategy().sendRedirect(request, response, "/home");
        super.onAuthenticationSuccess(request, response, chain, authentication);
        System.out.println("로그이 ㄴ성공");
    }

}