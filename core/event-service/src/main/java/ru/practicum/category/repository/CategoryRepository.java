package ru.practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
