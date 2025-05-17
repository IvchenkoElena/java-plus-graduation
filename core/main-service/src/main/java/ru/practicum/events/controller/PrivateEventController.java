package ru.practicum.events.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.events.dto.EventCreateDto;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.EventUpdateDto;
import ru.practicum.events.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventDto> getEvents(@PathVariable("userId") Long userId,
                                    @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam(defaultValue = "10") int size) {
        return eventService.privateUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@PathVariable("userId") Long userId, @Valid @RequestBody EventCreateDto eventDto) {
        return eventService.privateEventCreate(userId, eventDto);
    }

    @GetMapping(path = "/{eventId}")
    public EventDto getEvent(@PathVariable("userId") Long userId, @PathVariable("eventId") Long eventId) {
        return eventService.privateGetUserEvent(userId, eventId);
    }

    @PatchMapping(path = "/{eventId}")
    public EventDto updateEvent(@PathVariable("userId") Long userId,
                                @PathVariable("eventId") Long eventId, @Valid @RequestBody EventUpdateDto eventDto) {
        return eventService.privateUpdateUserEvent(userId, eventId, eventDto);
    }
}
