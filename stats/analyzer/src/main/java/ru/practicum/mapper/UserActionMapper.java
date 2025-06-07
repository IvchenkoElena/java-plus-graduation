package ru.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.UserAction;

@Slf4j
@Component
public class UserActionMapper {
    @Value("${application.action-weight.view}")
    private Double view;
    @Value("${application.action-weight.register}")
    private Double register;
    @Value("${application.action-weight.like}")
    private Double like;

    public UserAction mapToUserAction(UserActionAvro avro) {
        UserAction userAction = new UserAction();
        userAction.setEventId(avro.getEventId());
        userAction.setUserId(avro.getUserId());
        userAction.setWeight(getWeight(avro.getActionType()));
        userAction.setTimestamp(avro.getTimestamp());
        log.info("Результат маппинга: {}", userAction);
        return userAction;
    }

    private Double getWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> view;
            case REGISTER -> register;
            case LIKE -> like;
        };
    }
}