package ru.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;

public interface UserActionProducer {

    Producer<String, SpecificRecordBase> getProducer();

    void stop();
}