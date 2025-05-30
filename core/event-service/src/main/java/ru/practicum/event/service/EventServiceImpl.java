package ru.practicum.event.service;

import com.querydsl.core.types.Predicate;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.CommentClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.StatClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.enums.CommentStatus;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.enums.RequestStatus;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.OperationForbiddenException;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.event.enums.AdminUpdateStateAction;
import ru.practicum.dto.event.EntityParam;
import ru.practicum.dto.event.EventAdminUpdateDto;
import ru.practicum.dto.event.EventCreateDto;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventUpdateDto;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.dto.event.SearchEventsParam;
import ru.practicum.dto.event.enums.UpdateStateAction;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.dto.event.enums.EventSort;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.predicates.EventPredicates;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;

    private final UserClient userClient;
    private final RequestClient requestClient;
    private final StatClient statClient;
    private final CommentClient commentClient;

    @Override
    public List<EventDto> adminEventsSearch(SearchEventsParam param) {
        Pageable pageable = PageRequest.of(param.getFrom(), param.getSize());
        Predicate predicate = EventPredicates.adminFilter(param);
        if (predicate == null) {
            return addAdvancedDataToList(eventRepository.findAll(pageable).stream().map(eventMapper::toDto).toList());
        } else {
            return addAdvancedDataToList(eventRepository.findAll(predicate, pageable).stream().map(eventMapper::toDto).toList());
        }
    }

    @Override
    @Transactional
    public EventDto adminEventUpdate(Long eventId, EventAdminUpdateDto eventUpdateDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        if (eventUpdateDto.getEventDate() != null && eventUpdateDto.getEventDate().isBefore(event.getCreatedOn().minusHours(1))) {
            throw new ValidationException("Event date cannot be before created date");
        }

        updateEventData(event, eventUpdateDto.getTitle(),
                eventUpdateDto.getAnnotation(),
                eventUpdateDto.getDescription(),
                eventUpdateDto.getCategory(),
                eventUpdateDto.getEventDate(),
                eventUpdateDto.getLocation(),
                eventUpdateDto.getPaid(),
                eventUpdateDto.getRequestModeration(),
                eventUpdateDto.getParticipantLimit());
        if (eventUpdateDto.getStateAction() != null) {
            if (!event.getState().equals(EventState.PENDING)) {
                throw new OperationForbiddenException("Can't reject not pending event");
            }
            if (eventUpdateDto.getStateAction().equals(AdminUpdateStateAction.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (eventUpdateDto.getStateAction().equals(AdminUpdateStateAction.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }
        event = eventRepository.save(event);
        return addAdvancedData(eventMapper.toDto(event));
    }

    @Override
    public List<EventShortDto> getEvents(EntityParam params) {
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new ValidationException("Start date can not be after end date");
            }
        }

        Predicate predicate = EventPredicates.publicFilter(params.getText(), params.getCategories(), rangeStart,
                rangeEnd, params.getPaid());
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize());

        List<Event> filteredEvents;
        if (predicate != null) {
            filteredEvents = eventRepository.findAll(predicate, pageable).toList();
        } else {
            filteredEvents = eventRepository.findAll(pageable).toList();
        }

        if (params.getOnlyAvailable()) {
            filteredEvents = filteredEvents.stream().filter(this::isEventAvailable).toList();
        }
        List<EventShortDto> eventDtos = addAdvancedDataToShortDtoList(filteredEvents
                .stream()
                .map(eventMapper::toEventShortDto)
                .toList());

        EventSort sort = params.getSort();
        if (sort != null) {
            switch (sort) {
                case EVENT_DATE ->
                        eventDtos.stream().sorted(Comparator.comparing(EventShortDto::getEventDate)).toList();
                case VIEWS -> eventDtos.stream().sorted(Comparator.comparing(EventShortDto::getViews)).toList();
            }
        }
        return eventDtos;
    }

    @Override
    public EventDto getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId)));
        return addAdvancedData(eventMapper.toDto(event));
    }

    @Override
    public EventDto getPublishedEvent(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId)));
        return addAdvancedData(eventMapper.toDto(event));
    }

    @Override
    public List<EventDto> privateUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        List<EventDto> list = eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(eventMapper::toDto)
                .toList();
         return addAdvancedDataToList(list);
    }

    @Override
    @Transactional
    public EventDto privateEventCreate(Long userId, EventCreateDto eventCreateDto) {
        if (eventCreateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(String
                    .format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s",
                            eventCreateDto.getEventDate()));
        }
        if(!userClient.exists(userId)) {
            throw new NotFoundException("User not found");
        }
        Event event = eventMapper.fromDto(eventCreateDto);
        event.setInitiatorId(userId);
        Category category = categoryRepository.findById(eventCreateDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        event.setCategory(category);
        locationRepository.save(event.getLocation());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);
        return addAdvancedData(eventMapper.toDto(event));
    }

    @Override
    public EventDto privateGetUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId)));
        return addAdvancedData(eventMapper.toDto(event));
    }

    @Override
    @Transactional
    public EventDto privateUpdateUserEvent(Long userId, Long eventId, EventUpdateDto eventUpdateDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId)));
        if (event.getState().equals(EventState.PUBLISHED) || event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new OperationForbiddenException("Only pending or canceled events can be changed");
        }
        updateEventData(event, eventUpdateDto.getTitle(),
                eventUpdateDto.getAnnotation(),
                eventUpdateDto.getDescription(),
                eventUpdateDto.getCategory(),
                eventUpdateDto.getEventDate(),
                eventUpdateDto.getLocation(),
                eventUpdateDto.getPaid(),
                eventUpdateDto.getRequestModeration(),
                eventUpdateDto.getParticipantLimit());
        if (eventUpdateDto.getStateAction() != null) {
            if (eventUpdateDto.getStateAction().equals(UpdateStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
            if (eventUpdateDto.getStateAction().equals(UpdateStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }
        event = eventRepository.save(event);
        return addAdvancedData(eventMapper.toDto(event));
    }

    private void updateEventData(Event event,
                                 String title,
                                 String annotation,
                                 String description,
                                 Long categoryId,
                                 LocalDateTime eventDate,
                                 LocationDto location,
                                 Boolean paid,
                                 Boolean requestModeration,
                                 Integer participantLimit) {
        if (title != null) {
            event.setTitle(title);
        }
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (eventDate != null) {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s",
                        eventDate));
            }
            event.setEventDate(eventDate);
        }
        if (location != null) {
            Location newLocation = locationRepository.save(locationMapper.toLocation(location));
            event.setLocation(newLocation);
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
    }

    private EventDto addAdvancedData(EventDto eventDto) {
        List<String> gettingUris = new ArrayList<>();
        gettingUris.add("/events/" + eventDto.getId());
        Long views = statClient.getStats(LocalDateTime.now().minusYears(1), LocalDateTime.now(), gettingUris, true)
                .stream().map(ViewStats::getHits).reduce(0L, Long::sum);
        eventDto.setViews(views);

        Event event = eventRepository.findById(eventDto.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventDto.getId())));

        Long confirmedRequests = requestClient.countRequestsByEventAndStatus(event.getId(), RequestStatus.CONFIRMED);

        eventDto.setConfirmedRequests(confirmedRequests);

        List<CommentDto> comments = commentClient.getByEventIdAndStatus(eventDto.getId(), CommentStatus.PUBLISHED);
        eventDto.setComments(comments);

        return eventDto;
    }

    private boolean isEventAvailable(Event event) {
        Long confirmedRequestsAmount = requestClient.countRequestsByEventAndStatus(event.getId(), RequestStatus.CONFIRMED);
        return event.getParticipantLimit() > confirmedRequestsAmount;
    }

    private List<EventDto> addAdvancedDataToList(List<EventDto> eventDtoList) {

        List<Long> idsList = eventDtoList.stream().map(EventDto::getId).toList();
        log.info("Список ID: " + idsList);
        List<ParticipationRequestDto> requests = requestClient.getListByEventIds(idsList);

        List<CommentDto> comments = commentClient.getAllByEventIdInAndStatus(idsList, CommentStatus.PUBLISHED);

        List<String> uris = eventDtoList.stream().map(dto -> "/events/" + dto.getId()).toList();
        List<ViewStats> viewStats = statClient.getStats(LocalDateTime.now().minusYears(1), LocalDateTime.now(), uris, false);

        List<EventDto> changedList = eventDtoList.stream()
                .peek(dto -> dto.setConfirmedRequests(requests.stream()
                        .filter(r -> Objects.equals(r.getEvent(), dto.getId()))
                        .count()))
                .peek(dto -> dto.setComments(comments.stream()
                        .filter(c -> Objects.equals(c.getEventId(), dto.getId()))
                        .toList()))
                .peek(dto -> dto.setViews(viewStats.stream()
                        .filter(v -> v.getUri().equals("/events/" + dto.getId()))
                        .map(ViewStats::getHits)
                        .reduce(0L, Long::sum)))
                .toList();

        return changedList;
    }

    private List<EventShortDto> addAdvancedDataToShortDtoList(List<EventShortDto> eventShortDtoList) {

        List<Long> idsList = eventShortDtoList.stream().map(EventShortDto::getId).toList();
        List<ParticipationRequestDto> requests = requestClient.getListByEventIds(idsList);

        List<CommentDto> comments = commentClient.getAllByEventIdInAndStatus(idsList, CommentStatus.PUBLISHED);

        List<String> uris = eventShortDtoList.stream().map(dto -> "/events/" + dto.getId()).toList();
        List<ViewStats> viewStats = statClient.getStats(LocalDateTime.now().minusYears(1), LocalDateTime.now(), uris, false);

        List<EventShortDto> changedList = eventShortDtoList.stream()
                .peek(dto -> dto.setConfirmedRequests(requests.stream()
                        .filter(r -> Objects.equals(r.getEvent(), dto.getId()))
                        .count()))
                .peek(dto -> dto.setViews(viewStats.stream()
                        .filter(v -> v.getUri().equals("/events/" + dto.getId()))
                        .map(ViewStats::getHits)
                        .reduce(0L, Long::sum)))
                .toList();

        return changedList;
    }
}
