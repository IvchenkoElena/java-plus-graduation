package ru.practicum.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface CollectorService {
    void sendUserAction(UserActionAvro userAction);
}