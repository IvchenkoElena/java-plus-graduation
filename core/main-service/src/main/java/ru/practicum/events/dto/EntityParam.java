package ru.practicum.events.dto;

import lombok.Data;
import ru.practicum.events.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EntityParam {

    private String text;
    private EventSort sort;
    private Integer from;
    private Integer size;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean paid;
    private Boolean onlyAvailable;

}
