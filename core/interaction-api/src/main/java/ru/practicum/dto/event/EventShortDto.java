package ru.practicum.dto.event;

import lombok.Data;
import ru.practicum.dto.category.CategoryDto;


@Data
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;

    private Long confirmedRequests;
    private double rating;

    private boolean paid;

    private CategoryDto category;

    private Long initiator;

    private String eventDate;
}