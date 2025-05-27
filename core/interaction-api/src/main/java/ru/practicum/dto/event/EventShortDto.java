package ru.practicum.dto.event;

import lombok.Data;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;


@Data
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;

    private Long confirmedRequests;
    private Long views;

    private boolean paid;

    private CategoryDto category;

    private UserShortDto initiator;

    private String eventDate;
}