package focandlol.api.member.controller;

import focandlol.api.member.service.KakaoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class OAuthController {

	private final KakaoService kakaoService;

	@GetMapping("/oauth2/code/kakao")
	public void kakaoLogin(
			@RequestParam("code") String accessCode,
			HttpServletResponse httpServletResponse
	) throws IOException {

		// 1. 인가 코드로 카카오에 access token 요청
		String accessToken = kakaoService.getAccessToken(accessCode);

		// 2. 프론트엔드 리다이렉트 URL에 accessToken을 쿼리 파라미터로 포함
		String redirect_uri = "https://www.growtogether.site/oauth/kakao?token=" + accessToken;


		// 3. 리다이렉트
		httpServletResponse.sendRedirect(redirect_uri);
	}
}
