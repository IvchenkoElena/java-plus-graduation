package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;
    private final HitMapper hitMapper;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit hit = hitMapper.toHit(hitDto);
        statRepository.save(hit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<ViewStats> projections;

        if (end.isBefore(start)) {
            throw new ValidationException(String.format("End date %s is before start date %s", end, start));
        }

        if (uris != null && !uris.isEmpty()) {
            if (unique) {
                projections = statRepository.getUniqueStatsWithHitsAndUris(start, end, uris);
            } else {
                projections = statRepository.getStatsWithHitsAndUris(start, end, uris);
            }
        } else {
            if (unique) {
                projections = statRepository.getUniqueStatsWithHits(start, end);
            } else {
                projections = statRepository.getStatsWithHits(start, end);
            }
        }

        return projections;
    }
}