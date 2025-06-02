package ru.practicum.kafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;
import ru.practicum.config.AppConfig;

import java.util.Properties;

@Slf4j
@Component
@Data
public class KafkaConfig {
    private final AppConfig appConfig;

    public KafkaConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public Properties getConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, appConfig.getConsumer().getGroupId());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, appConfig.getConsumer().getClientId());
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.getConsumer().getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, appConfig.getConsumer().getKeyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, appConfig.getConsumer().getValueDeserializer());
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, appConfig.getConsumer().getMaxPollRecords());
        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, appConfig.getConsumer().getFetchMaxBytes());
        properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, appConfig.getConsumer().getMaxPartitionFetchBytes());
        return properties;
    }

    public Properties getProducerProperties() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                appConfig.getProducer().getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                appConfig.getProducer().getKeySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                appConfig.getProducer().getValueSerializer());
        log.info("Подготовили конфиг кафка продьюсера = {}", config);
        return config;
    }
}