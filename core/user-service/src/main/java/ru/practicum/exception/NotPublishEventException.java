package ru.practicum.exception;

public class NotPublishEventException extends RuntimeException {
    public NotPublishEventException(final String message) {
        super(message);
    }
}
