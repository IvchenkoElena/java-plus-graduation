package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ToString
@ConfigurationProperties("spring.kafka")
public class AppConfig {
    ProducerSettings producer;
    ConsumerSettings consumer;
    TopicsSettings topics;

    @Setter
    @Getter
    @ToString

    public static class ProducerSettings {
        private String bootstrapServers;
        private String keySerializer;
        private String valueSerializer;
    }

    @Setter
    @Getter
    @ToString
    public static class ConsumerSettings {
        private String bootstrapServers;
        private String keyDeserializer;
        private String valueDeserializer;
        private String clientId;
        private String groupId;
        private String maxPollRecords;
        private String fetchMaxBytes;
        private String maxPartitionFetchBytes;
        private Duration consumeAttemptsTimeoutMs;
    }

    @ToString
    @Getter
    @Setter
    public static class TopicsSettings {
        private String actionTopic;
        private String similarityTopic;
    }
}