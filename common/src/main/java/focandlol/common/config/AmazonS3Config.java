package focandlol.common.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Getter
@Setter
@RequiredArgsConstructor
public class AmazonS3Config {
    private final Credentials credentials;



    @Bean
    public AmazonS3 amazonS3() {
        if (credentials.accessKey == null || credentials.secretKey == null || credentials.region == null) {
            throw new IllegalArgumentException("AWS Access Key, Secret Key 또는 Region이 설정되지 않았습니다.");
        }

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(credentials.accessKey, credentials.secretKey);

        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(credentials.region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }
    @Getter
    @Setter
    @Component
    public static class Credentials {
        @Value("${cloud.aws.credentials.access-key}")
        private String accessKey;

        @Value("${cloud.aws.credentials.secret-key}")
        private String secretKey;

        @Value("${cloud.aws.credentials.region}")
        private String region;
    }
}
