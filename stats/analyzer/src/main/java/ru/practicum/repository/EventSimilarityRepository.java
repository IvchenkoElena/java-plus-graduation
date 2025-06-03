package ru.practicum.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EventSimilarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    Boolean existsByEventAAndEventB(Long eventA, Long eventB);

    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);

    List<EventSimilarity> findAllByEventAInOrEventBIn(Set<Long> eventAIds, Set<Long> eventBIds, PageRequest pageRequest);

    List<EventSimilarity> findAllByEventA(Long eventId, PageRequest pageRequest);

    List<EventSimilarity> findAllByEventB(Long eventId, PageRequest pageRequest);
}