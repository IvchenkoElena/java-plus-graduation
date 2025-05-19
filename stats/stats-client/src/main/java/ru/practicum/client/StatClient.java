package ru.practicum.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "stats-server")
public interface StatClient {
    String PATTERN = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")

    void saveHit(@Valid @RequestBody EndpointHitDto hitDto);

    @GetMapping("/stats")
    List<ViewStats> getStats(@RequestParam @DateTimeFormat(pattern = PATTERN) LocalDateTime start,
                                    @RequestParam @DateTimeFormat(pattern = PATTERN) LocalDateTime end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(defaultValue = "false") Boolean unique);
}
