package ru.practicum.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiError {
    String error;
    String description;

    public ApiError(String error, String description) {
        this.error = error;
        this.description = description;
    }

    public ApiError(HttpStatus status, String error, String description, String stackTrace) {
        this.error = error;
        this.description = description;
    }
}