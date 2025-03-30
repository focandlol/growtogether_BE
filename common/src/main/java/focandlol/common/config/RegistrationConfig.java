package focandlol.common.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public record RegistrationConfig(

        String clientId,
        String clientSecret,
        String redirectUri,
        List<String> scope,
        String authorizationGrantType

) {
}