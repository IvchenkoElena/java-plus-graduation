package ru.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {

    private final Map<Long, Map<Long, Double>> eventUserWeightMap = new HashMap<>();
    private final Map<Long, Double> eventSum = new HashMap<>();
    private final Map<Long, Map<Long, Double>> eventMinSum = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> getSimilarities(UserActionAvro actionAvro) {
        log.info("Сервис AggregatorServiceImpl.getSimilarities");
        long eventId = actionAvro.getEventId();
        double newScore = getActionScore(actionAvro);

        double currentWeight = 0.0;
        if (eventUserWeightMap.containsKey(eventId)) {
            currentWeight = eventUserWeightMap.get(eventId).get(eventId);
        } else {
            currentWeight = newScore;
            eventUserWeightMap.put(eventId, new HashMap<>());
        }
        if (currentWeight >= newScore) {
            log.info("Старый вес равен или больше нового, возвращаем пустоту");
            return List.of();
        }

        double newEventScoreSum;

        newEventScoreSum = eventSum.getOrDefault(eventId, 0.0) - currentWeight + newScore;
        eventSum.put(eventId, newEventScoreSum);


        Map<Long, Double> eventsToRecalculate = getLongDoubleMap(actionAvro, eventId);

        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Map.Entry<Long, Double> event2 : eventsToRecalculate.entrySet()) {
            double minSum = getMinScore(eventId, event2.getKey());
            double deltaMin = Math.min(newScore, event2.getValue()) - Math.min(currentWeight, event2.getValue());
            if (deltaMin != 0) {
                minSum += deltaMin;
                putMinWeights(eventId, event2.getKey(), minSum);
            }

            double event2Sum = eventSum.get(event2.getKey());
            float score = (float) (minSum / Math.sqrt(newEventScoreSum) / Math.sqrt(event2Sum));

            similarities.add(
                    EventSimilarityAvro.newBuilder()
                            .setEventA(Math.min(eventId, event2.getKey()))
                            .setEventB(Math.max(eventId, event2.getKey()))
                            .setTimestamp(actionAvro.getTimestamp())
                            .setScore(score)
                            .build()
            );
        }
        log.info("Новый вес {}", similarities);

        return similarities;
    }

    private Map<Long, Double> getLongDoubleMap(UserActionAvro action, long eventId) {
        Map<Long, Double> eventsToRecalculate = new HashMap<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : eventUserWeightMap.entrySet()) {
            Long currentEventId = entry.getKey();
            Map<Long, Double> userWeights = entry.getValue();

            if (!currentEventId.equals(eventId) && userWeights.containsKey(action.getUserId())) {
                Double weight = userWeights.get(action.getUserId());
                eventsToRecalculate.put(currentEventId, weight);
            }
        }
        return eventsToRecalculate;
    }

    private void putMinWeights(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        if (!eventMinSum.containsKey(first)) {
            eventMinSum.put(first, new HashMap<>());
        }

        Map<Long, Double> innerMap = eventMinSum.get(first);
        innerMap.put(second, sum);
    }

    private double getMinScore(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        Double value;
        Map<Long, Double> innerMap;

        if (!eventMinSum.containsKey(first)) {
            innerMap = new HashMap<>();
            eventMinSum.put(first, innerMap);
        } else {
            innerMap = eventMinSum.get(first);
        }

        value = innerMap.getOrDefault(second, 0.0);

        return value;
    }

    private double getActionScore(UserActionAvro action) {
        return switch (action.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}