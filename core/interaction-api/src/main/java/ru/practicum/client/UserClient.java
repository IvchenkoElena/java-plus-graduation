package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping
    public List<UserShortDto> get(@RequestParam final List<Long> ids);
    @GetMapping("/exists/{id}")
    public boolean exists(@PathVariable final long id);
}
