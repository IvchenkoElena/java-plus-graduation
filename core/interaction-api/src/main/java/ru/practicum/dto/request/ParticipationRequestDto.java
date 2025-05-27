package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ParticipationRequestDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long event;

    private Long requester;

    private String created;

    private String status;

}
