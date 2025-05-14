package ru.practicum.common.exception;


public class OperationForbiddenException extends RuntimeException {
    public OperationForbiddenException(final String message) {
        super(message);
    }
}
