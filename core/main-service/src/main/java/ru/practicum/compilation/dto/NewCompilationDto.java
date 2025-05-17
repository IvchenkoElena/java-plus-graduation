package ru.practicum.compilation.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NewCompilationDto {

    @Nullable
    private List<Long> events;
    private boolean pinned = false;

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
}
