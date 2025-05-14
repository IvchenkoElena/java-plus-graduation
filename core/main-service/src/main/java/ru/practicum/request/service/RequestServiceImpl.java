package ru.practicum.request.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.common.exception.InitiatorRequestException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.common.exception.NotPublishEventException;
import ru.practicum.common.exception.OperationUnnecessaryException;
import ru.practicum.common.exception.ParticipantLimitException;
import ru.practicum.common.exception.RepeatableUserRequestException;
import ru.practicum.common.exception.ValidationException;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventState;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId, HttpServletRequest request) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(String.format("User with id %s not found",
                userId)));
        return requestRepository.findByRequesterId(userId).stream()
                .map(requestMapper::requestToParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        if (eventRepository.findByIdAndInitiator_Id(eventId, userId).isPresent()) {
            throw new InitiatorRequestException(String.format("User with id %s is initiator for event with id %s",
                    userId, eventId));
        }
        if (!requestRepository.findByRequesterIdAndEventId(userId, eventId).isEmpty()) {
            throw new RepeatableUserRequestException(String.format("User with id %s already make request for event with id %s",
                    userId, eventId));
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(String.format(
                "Event with id %s not found", eventId)));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotPublishEventException(String.format("Event with id %s is not published", eventId));
        }
        Request request = new Request();
        User requester = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(String.format("User with id %s not found",
                userId)));
        request.setRequester(requester);
        request.setEvent(event);

        Long confirmedRequestsAmount = requestRepository.countRequestsByEventAndStatus(event, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() <= confirmedRequestsAmount && event.getParticipantLimit() != 0) {
            throw new ParticipantLimitException(String.format("Participant limit for event with id %s id exceeded", eventId));
        }

        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            request.setCreatedOn(LocalDateTime.now());
            return requestMapper.requestToParticipationRequestDto(requestRepository.save(request));
        }

        if (event.isRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
            request.setCreatedOn(LocalDateTime.now());
            return requestMapper.requestToParticipationRequestDto(requestRepository.save(request));
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
            request.setCreatedOn(LocalDateTime.now());
        }
        return requestMapper.requestToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request cancellingRequest = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id %s not found or unavailable " +
                        "for user with id %s", requestId, userId)));
        cancellingRequest.setStatus(RequestStatus.CANCELED);
        cancellingRequest = requestRepository.save(cancellingRequest);
        return requestMapper.requestToParticipationRequestDto(cancellingRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId, HttpServletRequest request) {
        List<Event> userEvents = eventRepository.findAllByInitiatorId(userId);
        Event event = userEvents.stream().filter(e -> e.getInitiator().getId().equals(userId)).findFirst()
                .orElseThrow(() -> new ValidationException(String.format("User with id %s is not initiator of event with id %s",
                        userId, eventId)));
        return requestRepository.findByEventId(event.getId())
                .stream()
                .map(requestMapper::requestToParticipationRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventStatusUpdate,
                                                              HttpServletRequest request) {
        Event event = eventRepository.findByIdAndInitiator_Id(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found " +
                        "or unavailable for user with id %s", eventId, userId)));
        int participantLimit = event.getParticipantLimit();
        if (participantLimit == 0 || !event.isRequestModeration()) {
            throw new OperationUnnecessaryException(String.format("Requests confirm for event with id %s is not required",
                    eventId));
        }

        List<Long> requestIds = eventStatusUpdate.getRequestIds();
        List<Request> requests = requestIds.stream()
                .map(r -> requestRepository.findByIdAndEventId(r, eventId)
                        .orElseThrow(() -> new ValidationException(String.format("Request with id %s is not apply " +
                                "to user with id %s or event with id %s", r, userId, eventId))))
                .toList();

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        long confirmedRequestsAmount;
        confirmedRequestsAmount = requestRepository.countRequestsByEventAndStatus(event, RequestStatus.CONFIRMED);
        if (confirmedRequestsAmount >= participantLimit) {
            throw new ParticipantLimitException(String.format("Participant limit for event with id %s id exceeded", eventId));
        }
        for (Request currentRequest : requests) {
            if (currentRequest.getStatus().equals(RequestStatus.PENDING)) {
                if (eventStatusUpdate.getStatus().equals(RequestStatus.CONFIRMED)) {
                    if (confirmedRequestsAmount <= participantLimit) {
                        currentRequest.setStatus(RequestStatus.CONFIRMED);
                        ParticipationRequestDto confirmed = requestMapper
                                .requestToParticipationRequestDto(currentRequest);
                        confirmedRequests.add(confirmed);
                    } else {
                        currentRequest.setStatus(RequestStatus.REJECTED);
                        ParticipationRequestDto rejected = requestMapper
                                .requestToParticipationRequestDto(currentRequest);
                        rejectedRequests.add(rejected);
                    }
                } else {
                    currentRequest.setStatus(eventStatusUpdate.getStatus());
                    ParticipationRequestDto rejected = requestMapper
                            .requestToParticipationRequestDto(currentRequest);
                    rejectedRequests.add(rejected);
                }
            }
        }
        requestRepository.saveAll(requests);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);
        return result;
    }

}
