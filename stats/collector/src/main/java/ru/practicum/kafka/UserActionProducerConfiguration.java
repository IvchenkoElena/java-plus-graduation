package ru.practicum.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class UserActionProducerConfiguration {
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;
    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Bean
    UserActionProducer getClient() {
        return new UserActionProducer() {

            private Producer<String, SpecificRecordBase> producer;

            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                if (producer == null) {
                    log.info(" Producer пустой, начинает создавть новый");
                    initProducer();
                }
                log.info("Возвращаем готовый продьюсер = {}", producer);
                return producer;
            }

            private void initProducer() {
                log.info("Начало инициализации продьюсера");
                Properties config = new Properties();
                log.info("BOOTSTRAP_SERVERS_CONFIG: {}",bootstrapServers);
                config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                        bootstrapServers);
                log.info("KEY_SERIALIZER_CLASS_CONFIG: {}",keySerializer);
                config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                        keySerializer);
                log.info("VALUE_SERIALIZER_CLASS_CONFIG: {}",valueSerializer);
                config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                        valueSerializer);
                log.info("Подготовили конфиг для продьюсера = {}", config.toString());

                producer = new KafkaProducer<>(config);
                log.info("Закончили инициализацию продьюсера = {}", producer.toString());
            }

            @PreDestroy
            @Override
            public void stop() {
                if (producer != null) {
                    producer.close();
                }
            }
        };
    }
}