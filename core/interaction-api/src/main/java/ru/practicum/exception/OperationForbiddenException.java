package ru.practicum.exception;


public class OperationForbiddenException extends RuntimeException {
    public OperationForbiddenException(final String message) {
        super(message);
    }
}
