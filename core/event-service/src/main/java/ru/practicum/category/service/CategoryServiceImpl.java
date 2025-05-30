package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.NotEmptyException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto saveCategory(NewCategoryDto dto) {
        Category category = mapper.toCategory(dto);
        category = categoryRepository.save(category);
        return mapper.toDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new NotEmptyException("Category with id=" + catId + " is not empty");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        Category categoryToUpdate = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
        categoryToUpdate.setName(dto.getName());
        categoryRepository.save(categoryToUpdate);
        return mapper.toDto(categoryToUpdate);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        PageRequest page = PageRequest.of(from, size);
        return categoryRepository.findAll(page).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
        return mapper.toDto(category);
    }
}
