package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.RequestClient;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.enums.RequestStatus;
import ru.practicum.service.RequestService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/internal/requests")
public class InternalRequestController implements RequestClient {
    private final RequestService requestService;

    @Override
    @GetMapping("/{requesterId}/{eventId}/{status}")
    public ParticipationRequestDto getByRequesterIdAndEventIdAndStatus(@PathVariable final Long requesterId,
                                                                       @PathVariable final Long eventId,
                                                                       @PathVariable final RequestStatus status) {
        return requestService.getByRequesterIdAndEventIdAndStatus(requesterId, eventId, status);
    }

    @Override
    @GetMapping("/{eventId}/{status}")
    public Long countRequestsByEventAndStatus(@PathVariable final Long eventId,
                                              @PathVariable final RequestStatus status) {
        return requestService.countRequestsByEventAndStatus(eventId, status);
    }

    @Override
    @GetMapping
    public List<ParticipationRequestDto> getListByEventIds(@RequestParam final List<Long> ids) {
        return requestService.getListByEventIds(ids);
    }
}
