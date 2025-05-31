package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class KafkaConsumerSettings {
    private String bootstrapServers;
    private String keyDeserializer;
    private String valueDeserializer;
    private String clientId;
    private String groupId;
    private String maxPollRecords;
    private int fetchMaxBytes;
    private int maxPartitionFetchBytes;
}