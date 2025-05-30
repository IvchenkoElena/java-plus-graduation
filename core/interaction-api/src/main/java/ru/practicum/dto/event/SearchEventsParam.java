package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.dto.event.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SearchEventsParam {
    private List<Long> users;
    private List<Long> categories;
    private List<EventState> states;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private int from;
    private int size;
}
