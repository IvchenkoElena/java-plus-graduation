package ru.practicum.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.events.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> { }
