package ru.practicum.dto;

import lombok.Data;
import ru.practicum.model.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;

    private RequestStatus status;

}
