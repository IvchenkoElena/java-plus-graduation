package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.EventClient;
import ru.practicum.dto.event.EventDto;
import ru.practicum.event.service.EventService;


@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/internal/events")
public class InternalEventController implements EventClient {
    private final EventService eventService;

    @Override
    @GetMapping("/{eventId}")
    public EventDto getById(@PathVariable final Long eventId) {
        return eventService.getEvent(eventId);
    }

    @Override
    @GetMapping("/{eventId}/{initiatorId}")
    public EventDto getByIdAndInitiatorId(@PathVariable final Long eventId, @PathVariable final Long initiatorId) {
        return eventService.privateGetUserEvent(initiatorId, eventId);
    }
}