package ru.practicum.compilation.dto;

import lombok.Data;
import ru.practicum.events.dto.EventShortDto;

import java.util.List;

@Data
public class CompilationDto {
    private Long id;
    private List<EventShortDto> events;
    private boolean pinned = false;
    private String title;
}
