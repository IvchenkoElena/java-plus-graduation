package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.UserActionMapper;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionHandler {

    private final UserActionRepository actionRepository;
    private final UserActionMapper actionMapper;

    public void handleUserAction(UserActionAvro avro) {
        log.info("Зашли в метод handleUserAction");
        UserAction action = actionMapper.mapToUserAction(avro);
        log.info("Начинаем проверку");

        if (!actionRepository.existsByEventIdAndUserId(action.getEventId(), action.getUserId())) {
            action = actionRepository.save(action);
            log.info("Сохраняем новое действие: {}", action);
        } else {
            UserAction oldAction = actionRepository
                    .findByEventIdAndUserId(action.getEventId(), action.getUserId()).get();
            log.info("Находим в БД старое действие: {}", oldAction);
            if (action.getWeight() > oldAction.getWeight()) {
                oldAction.setWeight(action.getWeight());
                oldAction.setTimestamp(action.getTimestamp());
                oldAction = actionRepository.save(oldAction);
                log.info("Вес действия увеличился, обновляем в БД: {}", oldAction);
            } else {
                log.info("Вес действия не увеличился, обновлять не нужно");
            }
        }
    }
}