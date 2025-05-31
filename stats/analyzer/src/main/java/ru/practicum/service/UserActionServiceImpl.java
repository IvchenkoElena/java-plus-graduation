package ru.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class UserActionServiceImpl implements UserActionService {
    private final UserActionRepository userActionRepository;

    @Override
    public void handleUserAction(UserActionAvro userActionAvro) {

        Optional<UserAction> userActionOptional = userActionRepository.findByUserIdAndEventId(userActionAvro.getUserId(),
                userActionAvro.getEventId());
        if (userActionOptional.isPresent()) {
            if (userActionOptional.get().getScore() <= calcInteractionScore(userActionAvro.getActionType())) {
                UserAction userAction = UserAction.builder()
                        .id(userActionOptional.get().getId())
                        .userId(userActionAvro.getUserId())
                        .timestamp(userActionAvro.getTimestamp())
                        .eventId(userActionAvro.getEventId())
                        .score(calcInteractionScore(userActionAvro.getActionType()))
                        .build();
                userActionRepository.save(userAction);
            }
        } else {
            UserAction userAction = UserAction.builder()
                    .userId(userActionAvro.getUserId())
                    .timestamp(userActionAvro.getTimestamp())
                    .eventId(userActionAvro.getEventId())
                    .score(calcInteractionScore(userActionAvro.getActionType()))
                    .build();
            userActionRepository.save(userAction);
        }
    }

    private double calcInteractionScore(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}