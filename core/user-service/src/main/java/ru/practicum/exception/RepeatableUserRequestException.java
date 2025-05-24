package ru.practicum.exception;

public class RepeatableUserRequestException extends RuntimeException {
    public RepeatableUserRequestException(final String message) {
        super(message);
    }
}
