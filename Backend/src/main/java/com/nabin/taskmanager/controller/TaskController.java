package com.nabin.taskmanager.controller;

import com.nabin.taskmanager.dto.TaskRequestDTO;
import com.nabin.taskmanager.dto.TaskResponseDTO;
import com.nabin.taskmanager.entities.TaskPriority;
import com.nabin.taskmanager.entities.TaskStatus;
import com.nabin.taskmanager.services.interfaces.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // ============================================
    // BASIC CRUD OPERATIONS (Your existing code)
    // ============================================

    /**
     * Get all tasks for a user (simple)
     * GET /api/tasks?userId=1
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(@RequestParam Long userId) {
        return ResponseEntity.ok(taskService.getAllTasksByUserId(userId));
    }

    /**
     * Get a specific task by ID
     * GET /api/tasks/1/2
     */
    @GetMapping("/{userId}/{taskId}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @PathVariable Long userId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(userId, taskId));
    }

    /**
     * Get tasks by status
     * GET /api/tasks/1/status?status=TODO
     */
    @GetMapping("/{userId}/status")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByStatus(
            @PathVariable Long userId,
            @RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(userId, status));
    }

    /**
     * Create a new task
     * POST /api/tasks?userId=1
     */
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(
            @RequestParam Long userId,
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(userId, taskRequestDTO));
    }

    /**
     * Update an existing task
     * PUT /api/tasks/1/2
     */
    @PutMapping("/{userId}/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        return ResponseEntity.ok(taskService.updateTask(userId, taskId, taskRequestDTO));
    }

    /**
     * Delete a task
     * DELETE /api/tasks/1/2
     */
    @DeleteMapping("/{userId}/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {
        taskService.deleteTask(userId, taskId);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    //  ADVANCED FEATURES (NEW)
    // ============================================

    /**
     * Get tasks with advanced filtering, sorting, and pagination
     *
     * Examples:
     * GET /api/tasks/filter?userId=1&status=TODO&priority=HIGH
     * GET /api/tasks/filter?userId=1&status=TODO&page=0&size=10&sort=dueDate,desc
     * GET /api/tasks/filter?userId=1&search=meeting&sort=priority,asc&sort=dueDate,desc
     * GET /api/tasks/filter?userId=1&dueDateFrom=2026-02-20T00:00:00&dueDateTo=2026-03-01T23:59:59
     *
     * @param userId User ID (required)
     * @param status Filter by status (optional)
     * @param priority Filter by priority (optional)
     * @param dueDateFrom Filter tasks from this date (optional)
     * @param dueDateTo Filter tasks until this date (optional)
     * @param search Search keyword in title/description (optional)
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sort Sort parameters (e.g., "dueDate,desc" or "priority,asc")
     * @return Paginated list of tasks
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksWithFilters(
            @RequestParam Long userId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        // Build Sort object from sort parameters
        Sort.Order[] orders = new Sort.Order[sort.length];
        for (int i = 0; i < sort.length; i++) {
            String[] sortParams = sort[i].split(",");
            String property = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            orders[i] = new Sort.Order(direction, property);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        Page<TaskResponseDTO> taskPage = taskService.getTasksWithFilters(
                userId, status, priority, dueDateFrom, dueDateTo, search, pageable
        );

        return ResponseEntity.ok(taskPage);
    }

    /**
     * Get overdue tasks for a user
     * GET /api/tasks/overdue?userId=1
     *
     * @param userId User ID
     * @return List of overdue tasks (not completed and past due date)
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponseDTO>> getOverdueTasks(@RequestParam Long userId) {
        List<TaskResponseDTO> tasks = taskService.getOverdueTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get tasks due soon (within specified days)
     * GET /api/tasks/due-soon?userId=1&days=7
     *
     * @param userId User ID
     * @param days Number of days ahead (default: 7)
     * @return List of tasks due within the specified timeframe
     */
    @GetMapping("/due-soon")
    public ResponseEntity<List<TaskResponseDTO>> getTasksDueSoon(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "7") int days) {

        List<TaskResponseDTO> tasks = taskService.getTasksDueSoon(userId, days);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get task statistics for a user
     * GET /api/tasks/stats?userId=1
     *
     * @param userId User ID
     * @return Statistics (total, by status, overdue count)
     */
    @GetMapping("/stats")
    public ResponseEntity<TaskStatsDTO> getTaskStats(@RequestParam Long userId) {
        // Create a simple stats DTO
        long totalTasks = taskService.getAllTasksByUserId(userId).size();
        long todoCount = taskService.getTasksByStatus(userId, TaskStatus.TODO).size();
        long inProgressCount = taskService.getTasksByStatus(userId, TaskStatus.IN_PROGRESS).size();
        long doneCount = taskService.getTasksByStatus(userId, TaskStatus.DONE).size();
        long overdueCount = taskService.getOverdueTasks(userId).size();

        TaskStatsDTO stats = new TaskStatsDTO(
                totalTasks, todoCount, inProgressCount, doneCount, overdueCount
        );

        return ResponseEntity.ok(stats);
    }

    // ============================================
    // INNER CLASS FOR STATS (Simple DTO)
    // ============================================
    /**
     * Simple DTO for task statistics
     * You can move this to a separate file if preferred
     */
    public record TaskStatsDTO(
            long totalTasks,
            long todoTasks,
            long inProgressTasks,
            long doneTasks,
            long overdueTasks
    ) {}
}