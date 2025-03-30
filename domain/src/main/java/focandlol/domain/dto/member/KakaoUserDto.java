package focandlol.domain.dto.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoUserDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @JsonProperty("properties")
    private Properties properties;

    @Data
    public static class KakaoAccount {
        private String email;
    }

    @Data
    public static class Properties {
        private String nickname;
        private String profile_image;
    }
}
