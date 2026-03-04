package com.nabin.workflow.controller;

import com.nabin.workflow.dto.common.ApiResponse;
import com.nabin.workflow.dto.response.UserResponseDTO;
import com.nabin.workflow.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;

    /**
     * Get all users (Admin only)
     * GET /api/admin/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can access
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        log.info("Admin endpoint: Getting all users");

        List<UserResponseDTO> users = userService.getAllUsers();

        ApiResponse<List<UserResponseDTO>> response = ApiResponse.success(
                String.format("Retrieved %d users", users.size()),
                users
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID (Admin only)
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        log.info("Admin endpoint: Getting user with ID: {}", id);

        UserResponseDTO user = userService.getUserById(id);

        ApiResponse<UserResponseDTO> response = ApiResponse.success(
                "User retrieved successfully",
                user
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete user (Admin only)
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.warn("Admin endpoint: Deleting user with ID: {}", id);

        userService.deleteUser(id);

        ApiResponse<Void> response = ApiResponse.success(
                "User deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all tasks across all users (Admin only)
     * GET /api/admin/tasks
     */
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getAllTasksAdmin() {
        log.info("Admin endpoint: Getting all tasks (all users)");

        ApiResponse<String> response = ApiResponse.success(
                "Admin tasks endpoint - implementation pending",
                "This would return all tasks from all users"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Test admin access
     * GET /api/admin/test
     */
    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> testAdminAccess() {
        log.info("Admin endpoint: Test access");

        ApiResponse<String> response = ApiResponse.success(
                "Admin access granted!",
                "You have ADMIN role"
        );

        return ResponseEntity.ok(response);
    }
}