package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.enums.RequestStatus;

import java.util.List;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestClient {
    @GetMapping("/{requesterId}/{eventId}/{status}")
    ParticipationRequestDto getByRequesterIdAndEventIdAndStatus(@PathVariable final Long requesterId,
                                                                @PathVariable final Long eventId,
                                                                @PathVariable final RequestStatus status);

    @GetMapping("/{eventId}/{status}")
    Long countRequestsByEventAndStatus(@PathVariable final Long eventId,
                                       @PathVariable final RequestStatus status);

    @GetMapping
    List<ParticipationRequestDto> getListByEventIds(@RequestParam final List<Long> ids);
}
