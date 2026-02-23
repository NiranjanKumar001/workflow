package com.nabin.taskmanager.controller;

import com.nabin.taskmanager.dto.CategoryDTO;
import com.nabin.taskmanager.services.interfaces.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Request;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor

public class CategoryController
{
    private final CategoryService categoryService;

    /**
     * Get all categories for a user
     * GET /api/categories?userId=1
     *
     * @param userid User ID
     * @return List of categories
     */

    @GetMapping
    public ResponseEntity <List<CategoryDTO>>getAllCategories(@RequestParam Long userId)
    {
        List<CategoryDTO>categories = categoryService.getAllCategoriesByUserId(userId);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get a single category by ID
     * GET /api/categories/{userId}/{categoryId}
     *
     * @param userId User ID
     * @param categoryId Category ID
     * @return Category details
     */

    @GetMapping("/{userId}/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @PathVariable Long userId,
            @PathVariable Long categoryId){
        CategoryDTO category = categoryService.getCategoryById(userId,categoryId);
        return ResponseEntity.ok(category);
    }

    /**
     * Create a new category
     * POST /api/categories?userId=1
     *
     * @param userId User ID
     * @param categoryDTO Category data
     * @return Created category
     */

  @PostMapping
    public ResponseEntity <CategoryDTO> createCategory (
           @RequestParam Long userId,
           @Valid @RequestBody CategoryDTO categoryDTO){
      CategoryDTO createdCategory = categoryService.createCategory(userId,categoryDTO);
      return ResponseEntity.ok(createdCategory);
  }

    /**
     * Update an existing category
     * PUT /api/categories/{userId}/{categoryId}
     *
     * @param userId User ID
     * @param categoryId Category ID
     * @param categoryDTO Updated category data
     * @return Updated category
     */

    @PutMapping("/{userId}/{categoryId}")
    public ResponseEntity <CategoryDTO> updateCategory(
            @PathVariable Long userId,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO updatedCategory = categoryService.updateCategory(userId,categoryId,categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Delete a category
     * DELETE /api/categories/{userId}/{categoryId}
     *
     * @param userId User ID
     * @param categoryId Category ID
     * @return No content
     */

    @DeleteMapping("/{userId}/{categoryId}")
    public ResponseEntity <Void> deleteCategory(
            @PathVariable Long userId,
            @PathVariable Long categoryId){
        categoryService.deleteCategory(userId,categoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Test endpoint to verify category controller is working
     * GET /api/categories/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Category Controller is working!");
    }

}
