package ru.practicum.event.service;

import ru.practicum.dto.event.EntityParam;
import ru.practicum.dto.event.EventAdminUpdateDto;
import ru.practicum.dto.event.EventCreateDto;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventRecommendationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventUpdateDto;
import ru.practicum.dto.event.SearchEventsParam;

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

    EventDto getPublishedEvent(Long id);

    void addLike(Long eventId, Long userId);

    List<EventRecommendationDto> getRecommendations(long userId);

}
