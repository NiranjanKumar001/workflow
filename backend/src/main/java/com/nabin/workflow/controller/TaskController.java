package com.nabin.workflow.controller;

import com.nabin.workflow.dto.common.ApiResponse;
import com.nabin.workflow.dto.common.PageMetadata;
import com.nabin.workflow.dto.request.TaskRequestDTO;
import com.nabin.workflow.dto.response.TaskResponseDTO;
import com.nabin.workflow.entities.TaskPriority;
import com.nabin.workflow.entities.TaskStatus;
import com.nabin.workflow.util.SecurityUtil;
import com.nabin.workflow.services.interfaces.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Create a new task
     * Gets userId from JWT token automatically
     * POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDTO>> createTask(
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {

        // Get userId from JWT token
        Long userId = SecurityUtil.getCurrentUserId();
        TaskResponseDTO task = taskService.createTask(userId, taskRequestDTO);

        ApiResponse<TaskResponseDTO> response = ApiResponse.success(
                "Task created successfully",
                task
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a specific task by ID
     * GET /api/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> getTaskById(
            @PathVariable Long taskId) {

        // Get userId from JWT token
        Long userId = SecurityUtil.getCurrentUserId();
        TaskResponseDTO task = taskService.getTaskById(userId, taskId);

        ApiResponse<TaskResponseDTO> response = ApiResponse.success(
                "Task retrieved successfully",
                task
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all tasks for current user
     * GET /api/tasks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getAllTasks() {

        // Get userId from JWT token
        Long userId = SecurityUtil.getCurrentUserId();
        List<TaskResponseDTO> tasks = taskService.getAllTasksByUserId(userId);

        ApiResponse<List<TaskResponseDTO>> response = ApiResponse.success(
                String.format("Retrieved %d tasks", tasks.size()),
                tasks
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get tasks with advanced filtering and pagination
     * GET /api/tasks/filter?status=TODO&page=0&size=10
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getTasksWithFilters(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateTo,
            @RequestParam(required = false) String search,
            Pageable pageable // <- Spring will parse page, size, sort automatically
    )
    {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<TaskResponseDTO> taskPage = taskService.getTasksWithFilters(
                userId, status, priority, dueDateFrom, dueDateTo, search, pageable);

        PageMetadata metadata = PageMetadata.from(taskPage);

        ApiResponse<List<TaskResponseDTO>> response = ApiResponse.success(
                String.format("Retrieved %d tasks (page %d of %d)",
                        taskPage.getNumberOfElements(),
                        taskPage.getNumber() + 1,
                        taskPage.getTotalPages()),
                taskPage.getContent(),
                metadata
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing task
     * PUT /api/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {

        // Get userId from JWT token
        Long userId = SecurityUtil.getCurrentUserId();
        TaskResponseDTO task = taskService.updateTask(userId, taskId, taskRequestDTO);

        ApiResponse<TaskResponseDTO> response = ApiResponse.success(
                "Task updated successfully",
                task
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a task
     * DELETE /api/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {

        // Get userId from JWT token
        Long userId = SecurityUtil.getCurrentUserId();
        taskService.deleteTask(userId, taskId);

        ApiResponse<Void> response = ApiResponse.success("Task deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get overdue tasks
     * GET /api/tasks/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getOverdueTasks() {

        // Get userId from JWT token
        Long userId = SecurityUtil.getCurrentUserId();

        List<TaskResponseDTO> tasks = taskService.getOverdueTasks(userId);
        ApiResponse<List<TaskResponseDTO>> response = ApiResponse.success(
                String.format("Found %d overdue tasks", tasks.size()),
                tasks
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get tasks due soon
     * GET /api/tasks/due-soon?days=7
     */
    @GetMapping("/due-soon")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getTasksDueSoon(
            @RequestParam(defaultValue = "7") int days) {

        // Get userId from JWT token
        Long userId = SecurityUtil.getCurrentUserId();
        List<TaskResponseDTO> tasks = taskService.getTasksDueSoon(userId, days);

        ApiResponse<List<TaskResponseDTO>> response = ApiResponse.success(
                String.format("Found %d tasks due in next %d days", tasks.size(), days),
                tasks
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get task statistics
     * GET /api/tasks/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TaskStatsDTO>> getTaskStats() {

        // Get userId from JWT token
        Long userId = SecurityUtil.getCurrentUserId();
        long totalTasks = taskService.getAllTasksByUserId(userId).size();
        long todoCount = taskService.getTasksByStatus(userId, TaskStatus.TODO).size();
        long inProgressCount = taskService.getTasksByStatus(userId, TaskStatus.IN_PROGRESS).size();
        long doneCount = taskService.getTasksByStatus(userId, TaskStatus.DONE).size();
        long overdueCount = taskService.getOverdueTasks(userId).size();

        TaskStatsDTO stats = new TaskStatsDTO(
                totalTasks, todoCount, inProgressCount, doneCount, overdueCount);

        ApiResponse<TaskStatsDTO> response = ApiResponse.success(
                "Task statistics retrieved successfully",
                stats
        );

        return ResponseEntity.ok(response);
    }

    public record TaskStatsDTO(
            long totalTasks,
            long todoTasks,
            long inProgressTasks,
            long doneTasks,
            long overdueTasks
    ) {}
}
