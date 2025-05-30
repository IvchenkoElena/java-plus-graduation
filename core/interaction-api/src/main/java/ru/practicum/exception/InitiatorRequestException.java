package ru.practicum.exception;

public class InitiatorRequestException extends RuntimeException {
    public InitiatorRequestException(final String message) {
        super(message);
    }
}
