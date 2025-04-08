package focandlol.consumer.config;

import focandlol.common.util.DeduplicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

  /**
   * Kafka에서 예외 발생 시 DLT(Dead Letter Topic)으로 메시지 전송
   */
  @Bean
  public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate,
      DeduplicationUtil deduplicationUtil) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
        kafkaTemplate,
        (record, ex) -> {
          String dedupKey = record.topic() + ":" + record.partition() + ":" + record.offset();
          if (deduplicationUtil.isDuplicate(dedupKey)) {
            return null;
          }
          return new TopicPartition(record.topic() + ".dlt", record.partition());
        }
    );

    return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      ConsumerFactory<String, String> consumerFactory,
      DefaultErrorHandler errorHandler
  ) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setCommonErrorHandler(errorHandler);

    factory.getContainerProperties().setAckMode(AckMode.MANUAL);

    return factory;
  }
}


