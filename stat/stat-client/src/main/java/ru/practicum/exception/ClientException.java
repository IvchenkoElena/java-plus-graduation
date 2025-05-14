package ru.practicum.exception;

import lombok.Getter;

public class ClientException extends RuntimeException {
    @Getter
    Integer status;
    String description;

    public ClientException(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

}
