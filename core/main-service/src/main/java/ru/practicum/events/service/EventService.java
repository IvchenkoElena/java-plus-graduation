package ru.practicum.events.service;

import ru.practicum.events.dto.EntityParam;
import ru.practicum.events.dto.EventAdminUpdateDto;
import ru.practicum.events.dto.EventCreateDto;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventUpdateDto;
import ru.practicum.events.dto.SearchEventsParam;

import java.util.List;

public interface EventService {

    List<EventDto> adminEventsSearch(SearchEventsParam searchEventsParam);

    EventDto adminEventUpdate(Long eventId, EventAdminUpdateDto eventDto);

    List<EventDto> privateUserEvents(Long userId, int from, int size);

    EventDto privateEventCreate(Long userId, EventCreateDto eventCreateDto);

    EventDto privateGetUserEvent(Long userId, Long eventId);

    EventDto privateUpdateUserEvent(Long userId, Long eventId, EventUpdateDto eventUpdateDto);

    List<EventShortDto> getEvents(EntityParam params);

    EventDto getEvent(Long eventId);

}
