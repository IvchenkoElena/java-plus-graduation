package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(pageable).stream().map(userMapper::toUserDto).toList();
        }
        return userRepository.findAllByIdIn(ids, pageable).stream().map(userMapper::toUserDto).toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User newUser = userMapper.toUser(userDto);
        newUser = userRepository.save(newUser);
        return userMapper.toUserDto(newUser);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        userRepository.deleteById(userId);
    }
}
