package ru.practicum.exception;

public class OperationUnnecessaryException extends RuntimeException {
    public OperationUnnecessaryException(final String message) {
        super(message);
    }
}
