package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.event.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    @Mapping(target = "id", ignore = true)
    Location toLocation(final LocationDto locationDto);

    LocationDto toLocationDto(final Location location);
}
