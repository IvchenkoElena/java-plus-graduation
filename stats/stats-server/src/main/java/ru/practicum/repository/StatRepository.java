package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.ViewStats;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.dto.ViewStats(s.app, s.uri, COUNT(s.id))" +
            "FROM EndpointHit s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.id) DESC")
    List<ViewStats> getStatsWithHits(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.ViewStats(s.app, s.uri, COUNT(s.id))" +
            "FROM EndpointHit s " +
            "WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.id) DESC")
    List<ViewStats> getStatsWithHitsAndUris(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.dto.ViewStats(s.app, s.uri, COUNT(distinct s.ip))" +
            "FROM EndpointHit s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(distinct s.ip) DESC")
    List<ViewStats> getUniqueStatsWithHits(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.ViewStats(s.app, s.uri, COUNT(distinct s.ip))" +
            "FROM EndpointHit s " +
            "WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(distinct s.ip) DESC")
    List<ViewStats> getUniqueStatsWithHitsAndUris(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("uris") List<String> uris);
}