package focandlol.common.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.kakao")
public record ProviderConfig(

        String authorizationUri,
        String tokenUri,
        String userInfoUri,
        String userNameAttribute

) {
}