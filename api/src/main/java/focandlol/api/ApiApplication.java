package focandlol.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "focandlol.domain",
    "focandlol.common",
    "focandlol.api"
})
@EnableJpaRepositories(basePackages = "focandlol.domain.repository")
@EntityScan(basePackages = "focandlol.domain.entity")
public class ApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiApplication.class, args);
  }

}
