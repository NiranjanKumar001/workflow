package com.nabin.workflow.services.interfaces;

import com.nabin.workflow.dto.request.CategoryRequestDTO;
import com.nabin.workflow.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO createCategory(Long userId, CategoryRequestDTO categoryRequestDTO);
    CategoryResponseDTO getCategoryById(Long userId, Long categoryId);
    List<CategoryResponseDTO> getAllCategoriesByUserId(Long userId);
    CategoryResponseDTO updateCategory(Long userId, Long categoryId, CategoryRequestDTO categoryRequestDTO);
    void deleteCategory(Long userId, Long categoryId);
}