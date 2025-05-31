package ru.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event_similarity")
public class EventSimilarity {
    @Id
    @Column(name = "event_a_id")
    private long eventA;
    @Column(name = "event_b_id")
    private long eventB;
    @Column(name = "score")
    private double score;
    private Instant timestamp;
}