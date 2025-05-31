package ru.practicum.kafka.producer;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.KafkaConfig;

import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
@Component
public class SimilarityEventProducerConfiguration {
    private final KafkaConfig kafkaConfig;

    @Bean
    SimilarityEventProducer getClient() {
        return new SimilarityEventProducer() {
            private Producer<String, SpecificRecordBase> producer;

            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                if (producer == null) {
                    initProducer();
                }
                return producer;
            }

            private void initProducer() {
                Properties config = kafkaConfig.getProducerProperties();
                producer = new KafkaProducer<>(config);
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