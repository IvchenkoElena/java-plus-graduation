package ru.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.comment.enums.CommentStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "author_id")
    private Long authorId;

    private LocalDateTime created = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private CommentStatus status = CommentStatus.PENDING;
}
