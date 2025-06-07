package ru.practicum.dto.event;

import lombok.Data;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.enums.EventState;


import java.util.List;

@Data
public class EventDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;

    private long confirmedRequests;
    private double rating;

    private boolean paid;
    private boolean requestModeration;
    private int participantLimit;

    private CategoryDto category;
    private LocationDto location;

    private EventState state;
    private Long initiator;

    private String eventDate;
    private String createdOn;
    private String publishedOn;

    private List<CommentDto> comments;
}
