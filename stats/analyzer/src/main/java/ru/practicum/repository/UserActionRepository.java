package ru.practicum.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.UserAction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    Boolean existsByEventIdAndUserId(Long eventId, Long userId);

    Optional<UserAction> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("select COALESCE(SUM(u.weight), 0) from UserAction as u where u.eventId = :eventId")
    Double countSumWeightByEventId(@Param("eventId") Long eventId);

    List<UserAction> findAllByUserId(Long userId, PageRequest pageRequest);

    List<UserAction> findAllByEventIdInAndUserId(Set<Long> viewedEvents, Long userId);
}