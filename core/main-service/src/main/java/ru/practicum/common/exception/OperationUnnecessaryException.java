package ru.practicum.common.exception;

public class OperationUnnecessaryException extends RuntimeException {
    public OperationUnnecessaryException(final String message) {
        super(message);
    }
}
