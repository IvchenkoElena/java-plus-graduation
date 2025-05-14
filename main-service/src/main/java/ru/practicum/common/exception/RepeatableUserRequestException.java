package ru.practicum.common.exception;

public class RepeatableUserRequestException extends RuntimeException {
    public RepeatableUserRequestException(final String message) {
        super(message);
    }
}
