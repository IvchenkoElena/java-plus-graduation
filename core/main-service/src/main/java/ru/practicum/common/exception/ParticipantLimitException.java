package ru.practicum.common.exception;

public class ParticipantLimitException extends RuntimeException {
    public ParticipantLimitException(final String message) {
        super(message);
    }
}
