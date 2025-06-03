package ru.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "analyzer.kafka.consumer.user-action.properties")
    public Properties getKafkaConsumerUserActionProperties() {
        log.info("{}: Создание Properties для UserAction", KafkaClientConfig.class.getSimpleName());
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "analyzer.kafka.consumer.event-similarity.properties")
    public Properties getKafkaConsumerEventSimilarityProperties() {
        log.info("{}: Создание Properties для EventSimilarity", KafkaClientConfig.class.getSimpleName());
        return new Properties();
    }

    @Bean
    KafkaClient getKafkaClient() {
        return new KafkaClient() {
            private Consumer<Long, UserActionAvro> kafkaUserActionConsumer;
            private Consumer<Long, EventSimilarityAvro> kafkaEventSimilarityConsumer;

            @Override
            public Consumer<Long, UserActionAvro> getKafkaUserActionConsumer() {
                log.info("{}: Создание UserActionConsumer", KafkaClientConfig.class.getSimpleName());
                kafkaUserActionConsumer = new KafkaConsumer<>(getKafkaConsumerUserActionProperties());
                return kafkaUserActionConsumer;
            }

            @Override
            public Consumer<Long, EventSimilarityAvro> getKafkaEventSimilarityConsumer() {
                log.info("{}: Создание EventSimilarityConsumer", KafkaClientConfig.class.getSimpleName());
                kafkaEventSimilarityConsumer = new KafkaConsumer<>(getKafkaConsumerEventSimilarityProperties());
                return kafkaEventSimilarityConsumer;
            }

            @Override
            public void close() {
                try {
                    kafkaUserActionConsumer.commitSync();
                    kafkaEventSimilarityConsumer.commitSync();
                } finally {
                    log.info("{}: Закрытие UserActionConsumer", KafkaClientConfig.class.getSimpleName());
                    kafkaUserActionConsumer.close();
                    log.info("{}: Закрытие EventSimilarityConsumer", KafkaClientConfig.class.getSimpleName());
                    kafkaEventSimilarityConsumer.close();
                }
            }
        };
    }
}