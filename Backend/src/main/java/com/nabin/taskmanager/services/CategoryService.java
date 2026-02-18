package com.nabin.taskmanager.services;

import com.nabin.taskmanager.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(Long userId, CategoryDTO categoryDTO);
    CategoryDTO getCategoryById(Long userId, Long categoryId);
    List<CategoryDTO> getAllCategoriesByUserId(Long userId);
    CategoryDTO updateCategory(Long userId, Long categoryId, CategoryDTO categoryDTO);
    void deleteCategory(Long userId, Long categoryId);
}