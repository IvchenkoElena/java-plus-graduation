package ru.practicum.kafka;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.config.KafkaConsumerSettings;

import java.util.Properties;

@Getter
@Configuration
public class ConfigKafkaProperties {
    KafkaConsumerSettings kafkaSettings;

    @Bean(name = "user-actions")
    @Qualifier("user-actions")
    @ConfigurationProperties(prefix = "spring.kafka.consumer-user-actions")
    protected KafkaConsumerSettings kafkaSnaphotkafkaConfig() {
        return new KafkaConsumerSettings();
    }

    @Bean(name = "events-similarity")
    @Qualifier("events-similarity")
    @ConfigurationProperties(prefix = "spring.kafka.consumer-events-similarity")
    protected KafkaConsumerSettings kafkaHubkafkaConfig() {
        return new KafkaConsumerSettings();
    }

    public Properties getSnapshotProperites() {
        kafkaSettings = kafkaSnaphotkafkaConfig();
        return getProperties(kafkaSettings);
    }

    public Properties getHubProperties() {
        kafkaSettings = kafkaHubkafkaConfig();
        return getProperties(kafkaSettings);
    }

    private Properties getProperties(KafkaConsumerSettings kafkaSettings) {
        Properties properties = new Properties();
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG,
                kafkaSettings.getClientId());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG,
                kafkaSettings.getGroupId());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaSettings.getBootstrapServers());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                kafkaSettings.getKeyDeserializer());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                kafkaSettings.getValueDeserializer());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                kafkaSettings.getMaxPollRecords());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MAX_BYTES_CONFIG,
                kafkaSettings.getFetchMaxBytes());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                kafkaSettings.getMaxPartitionFetchBytes());
        return properties;
    }
}