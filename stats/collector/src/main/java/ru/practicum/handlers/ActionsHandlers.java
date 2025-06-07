package ru.practicum.handlers;

import ru.practicum.grpc.stats.actions.UserActionProto;

public interface ActionsHandlers {
    void handle(UserActionProto userActionProto);
}