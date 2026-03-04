package com.nabin.workflow.services.interfaces;

import com.nabin.workflow.dto.request.CategoryRequestDTO;

import java.util.List;

public interface CategoryService {
    CategoryRequestDTO createCategory(Long userId, CategoryRequestDTO categoryRequestDTO);
    CategoryRequestDTO getCategoryById(Long userId, Long categoryId);
    List<CategoryRequestDTO> getAllCategoriesByUserId(Long userId);
    CategoryRequestDTO updateCategory(Long userId, Long categoryId, CategoryRequestDTO categoryRequestDTO);
    void deleteCategory(Long userId, Long categoryId);
}