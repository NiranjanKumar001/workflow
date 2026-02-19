package com.nabin.taskmanager.services.impl;

import com.nabin.taskmanager.dto.CategoryDTO;
import com.nabin.taskmanager.entities.Category;
import com.nabin.taskmanager.entities.User;
import com.nabin.taskmanager.exception.ResourceNotFoundException;
import com.nabin.taskmanager.mapper.DTOMapper;
import com.nabin.taskmanager.repository.CategoryRepository;
import com.nabin.taskmanager.repository.UserRepository;
import com.nabin.taskmanager.services.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final DTOMapper dtoMapper;

    @Override
    public CategoryDTO createCategory(Long userId, CategoryDTO categoryDTO) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Create category
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .color(categoryDTO.getColor())
                .user(user)
                .build();

        // Save category
        Category savedCategory = categoryRepository.save(category);

        // Convert to DTO and return
        return dtoMapper.toCategoryDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        return dtoMapper.toCategoryDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategoriesByUserId(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        List<Category> categories = categoryRepository.findByUserId(userId);

        return categories.stream()
                .map(dtoMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(Long userId, Long categoryId, CategoryDTO categoryDTO) {
        // Find category and verify ownership
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Update category fields
        category.setName(categoryDTO.getName());
        category.setColor(categoryDTO.getColor());

        // Save updated category
        Category updatedCategory = categoryRepository.save(category);

        // Convert to DTO and return
        return dtoMapper.toCategoryDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        // Find category and verify ownership
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Delete category
        categoryRepository.delete(category);
    }
}