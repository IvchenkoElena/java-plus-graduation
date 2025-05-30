package ru.practicum.exception;

public class ParticipantLimitException extends RuntimeException {
    public ParticipantLimitException(final String message) {
        super(message);
    }
}
