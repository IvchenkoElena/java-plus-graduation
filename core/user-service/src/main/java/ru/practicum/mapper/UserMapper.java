package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.User;

import java.util.List;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

    User toUser(UserDto userDto);

    List<UserShortDto> mapToShortDto(List<UserDto> users);
}
