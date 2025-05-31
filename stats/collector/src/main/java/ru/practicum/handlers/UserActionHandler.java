package ru.practicum.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.actions.ActionTypeProto;
import ru.practicum.grpc.stats.actions.UserActionProto;
import ru.practicum.service.CollectorService;

import java.time.Instant;

@Slf4j
@Component
public class UserActionHandler implements ActionsHandlers {
    CollectorService collectorService;

    public UserActionHandler(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @Override
    public void handle(UserActionProto userActionProto) {
        log.info("Обработчик UserActionHandler начал работать");

        log.info("На вход:{}", userActionProto.toString());
        UserActionAvro userActionAvro = new UserActionAvro();

        userActionAvro.setUserId(userActionProto.getUserId());
        log.info("Установили userId={}", userActionAvro.getUserId());

        userActionAvro.setActionType(getActionType(userActionProto.getActionType()));
        log.info("Установили setActionType={}", userActionAvro.getActionType());

        userActionAvro.setEventId(userActionProto.getEventId());
        log.info("Установили EventId={}", userActionAvro.getEventId());

        userActionAvro.setTimestamp(Instant.ofEpochSecond(userActionProto.getTimestamp().getSeconds(),
                userActionProto.getTimestamp().getNanos()));
        log.info("Установили timestamp={}", userActionAvro.getTimestamp());


        log.info("Смапили действие пользователя в AVRO {}", userActionAvro.toString());
        collectorService.sendUserAction(userActionAvro);


    }

    private ActionTypeAvro getActionType(ActionTypeProto actionTypeProto) {
        if (actionTypeProto.equals(ActionTypeProto.ACTION_LIKE)) {
            return ActionTypeAvro.LIKE;
        }
        if (actionTypeProto.equals(ActionTypeProto.ACTION_REGISTER)) {
            return ActionTypeAvro.REGISTER;
        }
        if (actionTypeProto.equals(ActionTypeProto.ACTION_VIEW)) {
            return ActionTypeAvro.VIEW;
        }
        return null;
    }
}