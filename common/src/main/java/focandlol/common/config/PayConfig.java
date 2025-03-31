package focandlol.common.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.pay")
public record PayConfig(

        String clientId,
        String secretKey,
        String readyUri,
        String approveUri

) {
}

