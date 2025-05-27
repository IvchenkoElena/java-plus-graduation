package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.event.EventDto;


@FeignClient(name = "event-service", path = "/internal/events")
public interface EventClient {
    @GetMapping("/{eventId}")
    EventDto getById(@PathVariable final Long eventId);
    @GetMapping("/{eventId}/{initiatorId}")
    EventDto getByIdAndInitiatorId(@PathVariable final Long eventId, @PathVariable final Long initiatorId);
}
