package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapperImpl;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.events.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapperImpl mapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto dto) {
        Compilation compilation = new Compilation();
        compilation.setPinned(dto.isPinned());
        compilation.setTitle(dto.getTitle());
        if (dto.getEvents() != null) {
            compilation.setEvents(dto.getEvents().stream().map(i -> eventRepository.findById(i)
                    .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", i)))).collect(Collectors.toSet()));
        }
        compilationRepository.save(compilation);
        return mapper.toDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilationToDelete = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation compilationToUpdate = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
        compilationToUpdate.setPinned(dto.isPinned());
        if (dto.getTitle() != null) {
            compilationToUpdate.setTitle(dto.getTitle());
        }
        if (dto.getEvents() != null) {
            compilationToUpdate.setEvents(dto.getEvents().stream().map(i -> eventRepository.findById(i)
                    .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", i)))).collect(Collectors.toSet()));
        }
        compilationRepository.save(compilationToUpdate);

        return mapper.toDto(compilationToUpdate);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from, size);
        if (pinned != null) {
            return compilationRepository.findByPinned(pinned, page).stream().map(mapper::toDto).toList();
        }
        return compilationRepository.findAll(page).stream().map(mapper::toDto).toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
        return mapper.toDto(compilation);
    }
}
