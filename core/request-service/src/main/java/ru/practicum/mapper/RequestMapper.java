package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.Request;

import java.time.format.DateTimeFormatter;

@Component
public class RequestMapper {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ParticipationRequestDto requestToParticipationRequestDto(Request request) {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(request.getId());
        requestDto.setEvent(request.getEventId());
        requestDto.setRequester(request.getRequesterId());
        requestDto.setCreated(dateTimeFormatter.format(request.getCreatedOn()));
        requestDto.setStatus(request.getStatus().name());
        return requestDto;
    }
}
