package ru.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.handler.EventSimilarityHandler;
import ru.practicum.kafka.KafkaClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EventSimilarityProcessor implements Runnable {
    private final Consumer<Long, EventSimilarityAvro> similarityConsumer;
    private final EventSimilarityHandler similarityHandler;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    @Value("${analyzer.kafka.topics.events-similarity}")
    private String eventsSimilarityTopic;

    public EventSimilarityProcessor(KafkaClient kafkaClient, EventSimilarityHandler similarityHandler) {
        this.similarityConsumer = kafkaClient.getKafkaEventSimilarityConsumer();
        this.similarityHandler = similarityHandler;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(similarityConsumer::wakeup));
        try {
            similarityConsumer.subscribe(List.of(eventsSimilarityTopic));
            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = similarityConsumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                        EventSimilarityAvro avro = record.value();

                        log.info("{}: отправка сообщения в handler", EventSimilarityProcessor.class.getSimpleName());
                        similarityHandler.handleEventSimilarity(avro);

                        manageOffsets(record, count, similarityConsumer);
                        count++;
                    }
                    similarityConsumer.commitAsync();
                }
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("{}: Ошибка во время обработки EventSimilarity", EventSimilarityProcessor.class.getSimpleName(), e);
        } finally {
            try {
                similarityConsumer.commitSync();
            } finally {
                log.info("{}: Закрываем консьюмер", EventSimilarityProcessor.class.getSimpleName());
                similarityConsumer.close();
            }
        }
    }

    private static void manageOffsets(
            ConsumerRecord<Long, EventSimilarityAvro> record,
            int count,
            Consumer<Long, EventSimilarityAvro> consumer
    ) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}