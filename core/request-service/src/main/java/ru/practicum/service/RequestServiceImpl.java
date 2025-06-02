package ru.practicum.service;

import com.google.protobuf.Timestamp;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.CollectorClient;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.enums.RequestStatus;
import ru.practicum.exception.InitiatorRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.NotPublishEventException;
import ru.practicum.exception.OperationUnnecessaryException;
import ru.practicum.exception.ParticipantLimitException;
import ru.practicum.exception.RepeatableUserRequestException;
import ru.practicum.exception.ValidationException;

import ru.practicum.grpc.stats.actions.ActionTypeProto;
import ru.practicum.grpc.stats.actions.UserActionProto;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;

import ru.practicum.repository.RequestRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final CollectorClient collectorClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId, HttpServletRequest request) {
        if(!userClient.exists(userId)) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }
        return requestRepository.findByRequesterId(userId).stream()
                .map(requestMapper::requestToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        Request request = new Request();
        beforeAddRequest(userId, eventId, request);
        return requestMapper.requestToParticipationRequestDto(requestRepository.save(request));
    }

    public Request beforeAddRequest(Long userId, Long eventId, Request request) {
        if (Objects.equals(eventClient.getById(eventId).getInitiator(), userId)) {
            throw new InitiatorRequestException(String.format("User with id %s is initiator for event with id %s",
                    userId, eventId));
        }
        if (!requestRepository.findByRequesterIdAndEventId(userId, eventId).isEmpty()) {
            throw new RepeatableUserRequestException(String.format("User with id %s already make request for event with id %s",
                    userId, eventId));
        }
        EventDto eventDto = eventClient.getById(eventId);
        if (!eventDto.getState().equals(EventState.PUBLISHED)) {
            throw new NotPublishEventException(String.format("Event with id %s is not published", eventId));
        }

        if(!userClient.exists(userId)) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }
        request.setRequesterId(userId);
        request.setEventId(eventId);

        Long confirmedRequestsAmount = requestRepository.countRequestsByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (eventDto.getParticipantLimit() <= confirmedRequestsAmount && eventDto.getParticipantLimit() != 0) {
            throw new ParticipantLimitException(String.format("Participant limit for event with id %s id exceeded", eventId));
        }

        if (eventDto.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            request.setCreatedOn(LocalDateTime.now());
            return request;
        }

        if (eventDto.isRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
            request.setCreatedOn(LocalDateTime.now());
            return request;
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
            request.setCreatedOn(LocalDateTime.now());
        }
        collectorClient.sendUserAction(createUserAction(eventId, userId, ActionTypeProto.ACTION_REGISTER, Instant.now()));
        return request;
    }

    private UserActionProto createUserAction(Long eventId, Long userId, ActionTypeProto type, Instant timestamp) {
        return UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(type)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano())
                        .build())
                .build();
    }

    @Override
    @Transactional
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
        EventDto eventDto = eventClient.getByIdAndInitiatorId(eventId, userId);
        return requestRepository.findByEventId(eventDto.getId())
                .stream()
                .map(requestMapper::requestToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventStatusUpdate,
                                                              HttpServletRequest request) {
        EventDto eventDto = eventClient.getByIdAndInitiatorId(eventId, userId);
        int participantLimit = eventDto.getParticipantLimit();
        if (participantLimit == 0 || !eventDto.isRequestModeration()) {
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
        confirmedRequestsAmount = requestRepository.countRequestsByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (confirmedRequestsAmount >= participantLimit) {
            throw new ParticipantLimitException(String.format("Participant limit for event with id %s id exceeded", eventId));
        }
        for (Request currentRequest : requests) {
            if (currentRequest.getStatus().equals(RequestStatus.PENDING)) {
                if (eventStatusUpdate.getStatus().equals(RequestStatus.CONFIRMED)) {
                    if (confirmedRequestsAmount < participantLimit) {
                        currentRequest.setStatus(RequestStatus.CONFIRMED);
                        ParticipationRequestDto confirmed = requestMapper
                                .requestToParticipationRequestDto(currentRequest);
                        confirmedRequests.add(confirmed);
                        confirmedRequestsAmount += 1;
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

    @Override
    public ParticipationRequestDto getByRequesterIdAndEventIdAndStatus(Long requesterId, Long eventId, RequestStatus status) {
        Optional<Request> mayBeRequest = requestRepository.findByRequesterIdAndEventIdAndStatus(requesterId, eventId, status);
        return mayBeRequest.map(requestMapper::requestToParticipationRequestDto).orElse(null);
    }

    @Override
    public Long countRequestsByEventAndStatus(Long eventId, RequestStatus status) {
        return requestRepository.countRequestsByEventIdAndStatus(eventId, status);
    }

    @Override
    public List<ParticipationRequestDto> getListByEventIds(List<Long> ids) {
        return requestRepository.findAllByEventIdIn(ids).stream()
                .map(requestMapper::requestToParticipationRequestDto)
                .toList();
    }
}