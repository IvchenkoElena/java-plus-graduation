package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserClient {
    @GetMapping
    List<UserShortDto> get(@RequestParam final List<Long> ids);
    @GetMapping("/exists/{id}")
    boolean exists(@PathVariable final Long id);
}
