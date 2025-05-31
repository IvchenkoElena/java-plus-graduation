package ru.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.EventSimilarityMapper;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {

    private final EventSimilarityRepository eventSimilarityRepository;
    private final EventSimilarityMapper eventSimilarityMapper;

    @Override
    public void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.info("сервис EventSimilarityServiceImpl начал обработку eventSimilarityAvro {}", eventSimilarityAvro);
        EventSimilarity eventSimilarity = eventSimilarityMapper.map(eventSimilarityAvro);
        Optional<EventSimilarity> eventSimilarityOptional = eventSimilarityRepository
                .findEventSimilaritiesByEventAAndEventB(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());

        if (eventSimilarityOptional.isPresent()) {
            eventSimilarityOptional.get().setScore(eventSimilarity.getScore());
            eventSimilarityOptional.get().setTimestamp(eventSimilarity.getTimestamp());
            eventSimilarityRepository.save(eventSimilarityOptional.get());

        } else {
            eventSimilarityRepository.save(eventSimilarity);
        }
    }
}