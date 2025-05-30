package ru.practicum.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationDto {
    @NotNull
    private Float lat;
    @NotNull
    private Float lon;
}
