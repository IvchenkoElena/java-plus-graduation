package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dto.request.enums.RequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "requester_id")
    private Long requesterId;

    @Column(name = "created_at")
    private LocalDateTime createdOn;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
