package ru.practicum.exception;

public class NotEmptyException extends RuntimeException {
    public NotEmptyException(final String message) {
        super(message);
    }
}
