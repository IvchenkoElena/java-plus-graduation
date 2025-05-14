package ru.practicum.events.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.events.dto.EventAdminUpdateDto;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.SearchEventsParam;
import ru.practicum.events.model.EventState;
import ru.practicum.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final EventService eventService;


    @GetMapping
    public List<EventDto> searchEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(name = "from", required = false, defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size
    ) {
        return eventService.adminEventsSearch(new SearchEventsParam(users, categories, states, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping(path = "{eventId}")
    public EventDto editEvent(@PathVariable("eventId") Long eventId, @Valid @RequestBody final EventAdminUpdateDto eventDto) {
        return eventService.adminEventUpdate(eventId, eventDto);
    }
}
