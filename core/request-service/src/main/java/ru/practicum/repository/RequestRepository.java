package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.request.enums.RequestStatus;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterId(Long requesterId);

    Optional<Request> findByIdAndRequesterId(Long id, Long requesterId);

    List<Request> findByEventId(Long eventId);

    List<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    Optional<Request> findByIdAndEventId(Long id, Long eventId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.eventId = :eventId and r.status = :status")
    long countRequestsByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventIdIn(List<Long> ids);

    Optional<Request> findByRequesterIdAndEventIdAndStatus(Long authorId, Long eventId, RequestStatus requestStatus);
}
