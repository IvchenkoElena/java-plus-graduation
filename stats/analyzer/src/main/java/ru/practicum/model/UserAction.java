package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder(toBuilder = true)
@Entity
@Getter
@Setter
@Table(name = "user_actions")
@AllArgsConstructor
@NoArgsConstructor
public class UserAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "event_id")
    private long eventId;
    @Column(name = "timestamp")
    private Instant timestamp;
    @Column(name = "score")
    private Double score;

}