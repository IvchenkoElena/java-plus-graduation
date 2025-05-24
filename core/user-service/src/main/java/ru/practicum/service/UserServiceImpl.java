package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

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

    @Override
    public boolean existsById(long id) {
        return userRepository.existsById(id);
    }
}
