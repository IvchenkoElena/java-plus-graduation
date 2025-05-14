package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.events.model.Event;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterId(Long requesterId);

    Optional<Request> findByIdAndRequesterId(Long id, Long requesterId);

    Optional<Request> findByEventId(Long eventId);

    List<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    Optional<Request> findByIdAndEventId(Long id, Long eventId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event = :event and r.status = :status")
    long countRequestsByEventAndStatus(Event event, RequestStatus status);

    List<Request> findAllByEventIdIn(List<Long> idsList);

    Optional<Request> findByRequesterIdAndEventIdAndStatus(long authorId, long eventId, RequestStatus requestStatus);
}
