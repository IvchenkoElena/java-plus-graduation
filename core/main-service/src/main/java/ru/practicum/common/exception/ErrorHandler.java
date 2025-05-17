package ru.practicum.common.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ValidationException.class, OperationUnnecessaryException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(final RuntimeException e) {
        log.warn("400 - BAD_REQUEST");
        return new ApiError("BAD_REQUEST", "Incorrectly made request.", e.getMessage(), LocalDateTime.now().toString());
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(RuntimeException e) {
        log.warn("404 - NOT_FOUND");
        return new ApiError("NOT_FOUND", "Entity not found", e.getMessage(), LocalDateTime.now().toString());
    }

    @ExceptionHandler({OperationForbiddenException.class, ParticipantLimitException.class, InitiatorRequestException.class,
            RepeatableUserRequestException.class, NotPublishEventException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleForbiddenError(RuntimeException e) {
        log.warn("409 - FORBIDDEN");
        return new ApiError("FORBIDDEN", "For the requested operation the conditions are not met.", e.getMessage(), LocalDateTime.now().toString());
    }

    @ExceptionHandler({DataIntegrityViolationException.class, NotEmptyException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictError(RuntimeException e) {
        log.warn("409 - CONFLICT_ERROR");
        return new ApiError("CONFLICT_ERROR", "Conflict error", e.getMessage(), LocalDateTime.now().toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServerError(RuntimeException e) {
        log.warn("500 - INTERNAL_SERVER_ERROR");
        return new ApiError("INTERNAL_SERVER_ERROR", "Internal server error", e.getMessage(), LocalDateTime.now().toString());
    }

}
