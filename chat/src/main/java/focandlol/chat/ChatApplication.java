package focandlol.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "focandlol.domain",
    "focandlol.common",
    "focandlol.chat",
    "focandlol.consumer"
})
@EnableJpaRepositories(basePackages = "focandlol.domain.repository")
@EntityScan(basePackages = "focandlol.domain.entity")
public class ChatApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatApplication.class, args);
  }

}
