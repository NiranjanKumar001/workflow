package com.nabin.workflow.controller;

import com.nabin.workflow.dto.common.ApiResponse;
import com.nabin.workflow.dto.request.CategoryRequestDTO;
import com.nabin.workflow.services.interfaces.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get all categories for a user
     * GET /api/categories?userId=1
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryRequestDTO>>> getAllCategories(
            @RequestParam Long userId) {

        List<CategoryRequestDTO> categories = categoryService.getAllCategoriesByUserId(userId);

        ApiResponse<List<CategoryRequestDTO>> response = ApiResponse.success(
                String.format("Retrieved %d categories", categories.size()),
                categories
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single category by ID
     * GET /api/categories/{userId}/{categoryId}
     */
    @GetMapping("/{userId}/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryRequestDTO>> getCategoryById(
            @PathVariable Long userId,
            @PathVariable Long categoryId) {

        CategoryRequestDTO category = categoryService.getCategoryById(userId, categoryId);

        ApiResponse<CategoryRequestDTO> response = ApiResponse.success(
                "Category retrieved successfully",
                category
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Create a new category
     * POST /api/categories?userId=1
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryRequestDTO>> createCategory(
            @RequestParam Long userId,
            @Valid @RequestBody CategoryRequestDTO categoryDTO) {

        CategoryRequestDTO createdCategory = categoryService.createCategory(userId, categoryDTO);

        ApiResponse<CategoryRequestDTO> response = ApiResponse.success(
                "Category created successfully",
                createdCategory
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing category
     * PUT /api/categories/{userId}/{categoryId}
     */
    @PutMapping("/{userId}/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryRequestDTO>> updateCategory(
            @PathVariable Long userId,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequestDTO categoryDTO) {

        CategoryRequestDTO updatedCategory = categoryService.updateCategory(userId, categoryId, categoryDTO);

        ApiResponse<CategoryRequestDTO> response = ApiResponse.success(
                "Category updated successfully",
                updatedCategory
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a category
     * DELETE /api/categories/{userId}/{categoryId}
     */
    @DeleteMapping("/{userId}/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long userId,
            @PathVariable Long categoryId) {

        categoryService.deleteCategory(userId, categoryId);

        ApiResponse<Void> response = ApiResponse.success("Category deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint
     * GET /api/categories/test
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        ApiResponse<String> response = ApiResponse.success(
                "Category Controller is working!",
                "System operational"
        );

        return ResponseEntity.ok(response);
    }
}