package ru.practicum.common.exception;

public class NotPublishEventException extends RuntimeException {
    public NotPublishEventException(final String message) {
        super(message);
    }
}
