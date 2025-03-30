package focandlol.api.member.service;

import focandlol.common.config.ProviderConfig;
import focandlol.common.config.RegistrationConfig;
import focandlol.domain.dto.member.KakaoTokenDto;
import focandlol.domain.dto.member.KakaoUserDto;
import focandlol.common.auth.util.JwtUtil;
import focandlol.domain.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KakaoService {

    private final RestTemplate restTemplate;
    private final MemberService memberService;

    private final RegistrationConfig registrationConfig;
    private final ProviderConfig providerConfig;

    private final JwtUtil jwtUtil;

    public String getAccessToken(String accessCode) {
        KakaoTokenDto kakaoToken = this.getKakaoToken(accessCode);
        KakaoUserDto kakaoUserInfo = this.getKakaoUserInfo(kakaoToken.getAccessToken());
        MemberEntity memberEntity = memberService.kakaoLogin(kakaoUserInfo);
        return jwtUtil.generateAccessToken(memberEntity.getEmail(),memberEntity.getMemberId(), memberEntity.getNickName());
    }

    // 카카오 Token 요청
    private KakaoTokenDto getKakaoToken(String accessCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", registrationConfig.clientId());
        params.add("redirect_url", registrationConfig.redirectUri());
        params.add("code", accessCode);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenDto> response = restTemplate.exchange(
                providerConfig.tokenUri(),
                HttpMethod.POST,
                kakaoTokenRequest,
                KakaoTokenDto.class);

        return response.getBody();
    }

    // 카카오 사용자 정보 가져오기
    private KakaoUserDto getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserDto> response = restTemplate.exchange(
                providerConfig.userInfoUri(),
                HttpMethod.GET,
                entity,
                KakaoUserDto.class);

        return response.getBody();
    }


}