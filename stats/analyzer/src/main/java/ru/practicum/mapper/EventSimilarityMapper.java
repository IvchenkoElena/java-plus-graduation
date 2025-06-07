package ru.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarity;

@Slf4j
@Component
public class EventSimilarityMapper {
    public EventSimilarity mapToEventSimilarity(EventSimilarityAvro avro) {
        EventSimilarity eventSimilarity = new EventSimilarity();
        eventSimilarity.setEventA(avro.getEventA());
        eventSimilarity.setEventB(avro.getEventB());
        eventSimilarity.setScore(avro.getScore());
        eventSimilarity.setTimestamp(avro.getTimestamp());
        log.info("Результат маппинга: {}", eventSimilarity);
        return eventSimilarity;
    }
}