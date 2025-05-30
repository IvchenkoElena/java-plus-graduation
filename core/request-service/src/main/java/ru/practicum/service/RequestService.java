package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.request.enums.RequestStatus;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;


import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId, HttpServletRequest request);

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId, HttpServletRequest request);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest eventStatusUpdate,
                                                       HttpServletRequest request);

    ParticipationRequestDto getByRequesterIdAndEventIdAndStatus(Long requesterId, Long eventId, RequestStatus status);

    Long countRequestsByEventAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequestDto> getListByEventIds(List<Long> ids);
}
