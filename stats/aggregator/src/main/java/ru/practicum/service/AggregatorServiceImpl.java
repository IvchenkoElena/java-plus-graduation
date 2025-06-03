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
    private Map<Long, Map<Long, Double>> eventUserWeight = new HashMap<>();
    private Map<Long, Double> eventWeightSum = new HashMap<>();
    private Map<Long, Map<Long, Double>> twoEventsMinSum = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> aggregationUserAction(UserActionAvro action) {
        List<EventSimilarityAvro> result = new ArrayList<>();
        Double weightDiff = getDiffEventUserWeight(action);
        if (weightDiff.equals(0.0)) {
            log.info("Вес действия пользователя не изменился, возвращаем пустой список");
            return new ArrayList<>();
        }
        updateEventUserWeight(action);
        updateEventWeightSum(action, weightDiff);

        List<Long> eventIdsForCalculate = new ArrayList<>();
        for (Long id : eventUserWeight.keySet()) {
            if (!id.equals(action.getEventId())) {
                Double otherEventUserWeight = eventUserWeight.get(id).get(action.getUserId());
                if (otherEventUserWeight != null && otherEventUserWeight != 0) {
                    eventIdsForCalculate.add(id);
                }
            }
        }
        if (eventIdsForCalculate.isEmpty()) {
            log.info("Не найдено событий для расчета сходства, возвращаем пустой список");
            return new ArrayList<>();
        }
        log.info("События для расчета сходства: {}", eventIdsForCalculate);

        updateTwoEventsMinSum(action, weightDiff, eventIdsForCalculate);

        log.info("Расчитываем сходство для события {}, с событиями: {}", action.getEventId(), eventIdsForCalculate);
        for (Long id : eventIdsForCalculate) {
            Long first = Math.min(action.getEventId(), id);
            Long second = Math.max(action.getEventId(), id);

            Double similarity = twoEventsMinSum.get(first).get(second) /
                    (Math.sqrt(eventWeightSum.get(first)) * Math.sqrt(eventWeightSum.get(second)));
            log.info("Схожесть события {} и {} равна: {}", first, second, similarity);
            EventSimilarityAvro eventSimilarity = EventSimilarityAvro.newBuilder()
                    .setEventA(first)
                    .setEventB(second)
                    .setScore(similarity)
                    .setTimestamp(action.getTimestamp())
                    .build();
            result.add(eventSimilarity);
        }

        log.info("Результат расчета: {}", result);
        return result;
    }

    private void updateEventWeightSum(UserActionAvro action, Double weightDiff) {
        Long userId = action.getUserId();
        Long eventId = action.getEventId();
        Double eventWeight = eventUserWeight.get(eventId).getOrDefault(userId, 0.0);
        log.info("Обновляем eventWeightSum для события: {}", eventId);
        if (weightDiff.equals(0.0)) {
            log.info("Вес события не изменился, расчет делать не нужно");
            return;
        }
        if (!eventWeightSum.containsKey(eventId)) {
            eventWeightSum.put(eventId, eventWeight);
            log.info("Событие новое, сумма будет равна eventWeight: {}", eventWeight);
            return;
        }
        Double newSum = eventWeightSum.merge(eventId, weightDiff, Double::sum);
        log.info("Новая сумма в eventWeightSum для события {}, равна: {}", eventId, newSum);
    }

    private void updateTwoEventsMinSum(UserActionAvro action, Double diffWeight, List<Long> eventIdsForCalculate) {
        Long userId = action.getUserId();
        Long eventId = action.getEventId();
        Double eventWeight = eventUserWeight.get(eventId).getOrDefault(userId, 0.0);
        log.info("Расчитываем минимальную сумму для события {}, с событиями: {}", eventId, eventIdsForCalculate);

        if (eventWeight.equals(0.0) || diffWeight.equals(0.0)) {
            log.info("Вес действия для события {} равен 0 или не изменился, не делаем расчет", eventId);
            return;
        }
        Double oldEventWeight = eventWeight - diffWeight;
        log.info("Старый вес: {}, для события {}, пользователя {}", oldEventWeight, eventId, userId);

        for (Long otherEventId : eventIdsForCalculate) {
            Double otherEventWeight = eventUserWeight.get(otherEventId).getOrDefault(userId, 0.0);
            log.info("Вес: {}, для события {}, пользователя {}", otherEventWeight, otherEventId, userId);
            if (otherEventWeight.equals(0.0)) {
                log.info("Вес действия для события {} равен 0, не делаем расчет", otherEventId);
                continue;
            }

            Long first = Math.min(eventId, otherEventId);
            Long second = Math.max(eventId, otherEventId);
            Map<Long, Double> map = twoEventsMinSum.get(first);
            if (map == null || map.isEmpty()) {
                twoEventsMinSum.computeIfAbsent(first, k -> new HashMap<>())
                        .put(second, Math.min(eventWeight, otherEventWeight));
                log.info("Минимальная сумма еще не расчитывалась, сохраняем новую: first - {}, second - {}, sum - {}",
                        first, second, Math.min(eventWeight, otherEventWeight));
                continue;
            }
            Double oldSum = map.get(second);
            log.info("Старая минимальная сумма {}, для событий {} и {}", oldSum, eventId, otherEventId);
            if (oldSum == null) {
                twoEventsMinSum.computeIfAbsent(first, k -> new HashMap<>())
                        .put(second, Math.min(eventWeight, otherEventWeight));
                log.info("Минимальная сумма еще не расчитывалась, сохраняем новую: first - {}, second - {}, sum - {}",
                        first, second, Math.min(eventWeight, otherEventWeight));
                continue;
            }

            if (eventWeight >= otherEventWeight) {
                log.info("eventWeight {} >= otherEventWeight {}", eventWeight, otherEventWeight);
                if (oldEventWeight >= otherEventWeight) {
                    log.info("oldEventWeight {} >= otherEventWeight {}, сумму обновлять не нужно", oldEventWeight, otherEventWeight);
                    continue;
                } else {
                    log.info("oldEventWeight {} < otherEventWeight {}", oldEventWeight, otherEventWeight);
                    oldSum += otherEventWeight - oldEventWeight;
                    log.info("Новая минимальная сумма: {}", oldSum);
                }
            } else {
                log.info("eventWeight {} < otherEventWeight {}", eventWeight, otherEventWeight);
                oldSum += eventWeight - oldEventWeight;
                log.info("Новая минимальная сумма: {}", oldSum);
            }
            twoEventsMinSum.computeIfAbsent(first, k -> new HashMap<>())
                    .put(second, oldSum);
            log.info("Обновляем минимальную сумму: first - {}, second - {}, sum - {}", first, second, oldSum);
        }
    }

    private Double getDiffEventUserWeight(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        Double weight = getWeight(action);
        log.info("Рассчитываем разницу между старым весом действия с событием {}, пользователем {}", eventId, userId);

        Map<Long, Double> oldUserWeight = eventUserWeight.get(eventId);
        if (oldUserWeight == null || oldUserWeight.isEmpty()) {
            log.info("Пользователи не совершали действий с событием {}, разница будет {}", eventId, weight);
            return weight;
        }
        Double oldWeight = oldUserWeight.get(userId);
        if (oldWeight == null || oldWeight == 0) {
            log.info("Вес действия пользователя {}, с событием {}, был 0, разница будет {}", userId, eventId, weight);
            return weight;
        }
        if (oldWeight >= weight) {
            log.info("Старый вес {} >= нового {}, разница будет 0", oldWeight, weight);
            return 0.0;
        }
        Double diff = weight - oldWeight;
        log.info("Новый вес {} - старый {} = {}", weight, oldWeight, diff);
        return diff;
    }

    private void updateEventUserWeight(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        Double weight = getWeight(action);
        log.info("Обновляем вес: {} в eventUserWeight, для события {} и пользователя {}", weight, eventId, userId);

        Map<Long, Double> oldUserWeight = eventUserWeight.get(eventId);
        if (oldUserWeight == null || oldUserWeight.isEmpty()) {
            log.info("Пользователи не совершали действий с событием {}, новый вес будет {}", eventId, weight);
            eventUserWeight.computeIfAbsent(eventId, k -> new HashMap<>()).put(userId, weight);
            log.info("Результат {}", eventUserWeight.get(eventId));
            return;
        }
        Double oldWeight = oldUserWeight.get(userId);
        if (oldWeight == null || oldWeight == 0) {
            log.info("Вес действия пользователя {}, с событием {}, был 0, новый вес будет {}", userId, eventId, weight);
            oldUserWeight.put(userId, weight);
            log.info("Результат {}", eventUserWeight.get(eventId));
            return;
        }
        if (oldWeight >= weight) {
            log.info("Старый вес {} >= нового {}, не обновляем", oldWeight, weight);
            return;
        }
        oldUserWeight.put(userId, weight);
        log.info("Результат {}", eventUserWeight.get(eventId));
    }

    private double getWeight(UserActionAvro action) {
        return switch (action.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}