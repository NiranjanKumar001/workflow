package com.nabin.workflow.services.impl;

import com.nabin.workflow.dto.request.CategoryRequestDTO;
import com.nabin.workflow.entities.Category;
import com.nabin.workflow.entities.User;
import com.nabin.workflow.exception.ResourceNotFoundException;
import com.nabin.workflow.exception.UnauthorizedException;
import com.nabin.workflow.mapper.DTOMapper;
import com.nabin.workflow.repository.CategoryRepository;
import com.nabin.workflow.repository.UserRepository;
import com.nabin.workflow.util.SecurityUtil;
import com.nabin.workflow.services.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final DTOMapper dtoMapper;

    @Override
    @PreAuthorize("isAuthenticated()")
    public CategoryRequestDTO createCategory(Long userId, CategoryRequestDTO categoryDTO) {
        validateUserOwnership(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = Category.builder()
                .name(categoryDTO.getName())
                .color(categoryDTO.getColor())
                .user(user)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created: {} by user: {}", savedCategory.getId(), userId);

        return dtoMapper.toCategoryDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public CategoryRequestDTO  getCategoryById(Long userId, Long categoryId) {
        validateUserOwnership(userId);

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        return dtoMapper.toCategoryDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<CategoryRequestDTO > getAllCategoriesByUserId(Long userId) {
        validateUserOwnership(userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        List<Category> categories = categoryRepository.findByUserId(userId);

        return categories.stream()
                .map(dtoMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public CategoryRequestDTO updateCategory(Long userId, Long categoryId, CategoryRequestDTO  categoryDTO) {
        validateUserOwnership(userId);

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        category.setName(categoryDTO.getName());
        category.setColor(categoryDTO.getColor());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated: {} by user: {}", updatedCategory.getId(), userId);

        return dtoMapper.toCategoryDTO(updatedCategory);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteCategory(Long userId, Long categoryId) {
        validateUserOwnership(userId);

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        categoryRepository.delete(category);
        log.info("Category deleted: {} by user: {}", categoryId, userId);
    }

    /**
     * Validate user ownership
     */
    private void validateUserOwnership(Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        if (!currentUserId.equals(userId)) {
            log.warn("User {} attempted to access data for user {}", currentUserId, userId);
            throw new UnauthorizedException("You don't have permission to access this user's data");
        }
    }
}