package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.UserClient;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.mapper.UserMapper;
import ru.practicum.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/internal/users")
public class InternalUserController implements UserClient {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserShortDto> get(@RequestParam final List<Long> ids
    ) {
        return userMapper.mapToShortDto(userService.getUsers(ids, Pageable.unpaged().getPageNumber(), Pageable.unpaged().getPageSize()));
    }

    @GetMapping("/exists/{id}")
    public boolean exists(@PathVariable final Long id) {
        return userService.existsById(id);
    }
}
