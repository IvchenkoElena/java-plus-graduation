package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.model.User;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

    User toUser(UserDto userDto);
}
