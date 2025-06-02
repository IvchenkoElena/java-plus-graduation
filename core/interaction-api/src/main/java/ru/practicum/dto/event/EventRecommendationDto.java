package ru.practicum.dto.event;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class EventRecommendationDto {

    private long eventId;
    private double score;
}