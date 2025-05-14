package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;

@Mapper(componentModel = "spring")
public interface HitMapper {

    @Mapping(target = "id", ignore = true)
    EndpointHit toHit(EndpointHitDto dto);
}