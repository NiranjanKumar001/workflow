package com.nabin.workflow.controller;

import com.nabin.workflow.dto.common.ApiResponse;
import com.nabin.workflow.dto.request.TaskFilterDTO;
import com.nabin.workflow.dto.request.TaskRequestDTO;
import com.nabin.workflow.dto.request.TaskUpdateDTO;
import com.nabin.workflow.dto.response.TaskResponseDTO;
import com.nabin.workflow.dto.response.TaskStatsDTO;
import com.nabin.workflow.entities.TaskStatus;
import com.nabin.workflow.services.interfaces.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    // -------------------------------------------------------
    // POST /api/tasks — Create task
    // FIX: was TaskRequestDTO, now TaskCreateDTO
    // -------------------------------------------------------
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> createTask(
            @Valid @RequestBody TaskRequestDTO taskDTO) {

        log.info("Creating new task: {}", taskDTO.getTitle());
        TaskResponseDTO task = taskService.createTask(taskDTO);
        return new ResponseEntity<>(
                ApiResponse.success("Task created successfully", task),
                HttpStatus.CREATED
        );
    }

    // -------------------------------------------------------
    // GET /api/tasks/stats — Task statistics
    // FIX: this endpoint was missing — caused "Failed to load dashboard data"
    // Must be declared BEFORE /{id} to avoid path ambiguity
    // -------------------------------------------------------
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TaskStatsDTO>> getTaskStats() {
        log.info("Fetching task stats for current user");
        TaskStatsDTO stats = taskService.getTaskStats();
        return ResponseEntity.ok(
                ApiResponse.success("Task stats retrieved successfully", stats)
        );
    }

    // -------------------------------------------------------
    // GET /api/tasks — Get all tasks
    // -------------------------------------------------------
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getAllTasks() {
        log.info("Fetching all tasks for current user");
        List<TaskResponseDTO> tasks = taskService.getAllTasksForCurrentUser();
        return ResponseEntity.ok(
                ApiResponse.success("Tasks retrieved successfully", tasks)
        );
    }

    // -------------------------------------------------------
    // GET /api/tasks/{id} — Get task by ID
    // -------------------------------------------------------
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> getTaskById(@PathVariable Long id) {
        log.info("Fetching task: {}", id);
        TaskResponseDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Task retrieved successfully", task)
        );
    }

    // -------------------------------------------------------
    // GET /api/tasks/status/{status} — Get tasks by status
    // -------------------------------------------------------
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getTasksByStatus(
            @PathVariable TaskStatus status) {
        log.info("Fetching tasks with status: {}", status);
        List<TaskResponseDTO> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success("Tasks retrieved successfully", tasks)
        );
    }

    // -------------------------------------------------------
    // GET /api/tasks/overdue — Get overdue tasks
    // Must be declared BEFORE /{id} to avoid path ambiguity
    // -------------------------------------------------------
    @GetMapping("/overdue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getOverdueTasks() {
        log.info("Fetching overdue tasks");
        List<TaskResponseDTO> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(
                ApiResponse.success("Overdue tasks retrieved successfully", tasks)
        );
    }

    // -------------------------------------------------------
    // GET /api/tasks/overdue/count — Count overdue tasks
    // -------------------------------------------------------
    @GetMapping("/overdue/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> countOverdueTasks() {
        log.info("Counting overdue tasks");
        Long count = taskService.countOverdueTasks();
        return ResponseEntity.ok(
                ApiResponse.success("Overdue task count retrieved successfully", count)
        );
    }

    // -------------------------------------------------------
    // GET /api/tasks/due-soon?days=7 — Get tasks due soon
    // -------------------------------------------------------
    @GetMapping("/due-soon")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getTasksDueSoon(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Fetching tasks due within {} days", days);
        List<TaskResponseDTO> tasks = taskService.getTasksDueSoon(days);
        return ResponseEntity.ok(
                ApiResponse.success("Tasks due soon retrieved successfully", tasks)
        );
    }

    // -------------------------------------------------------
    // PUT /api/tasks/{id} — Full update
    // -------------------------------------------------------
    @PutMapping("/{id:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateDTO taskDTO) {
        log.info("Updating task: {}", id);
        TaskResponseDTO task = taskService.updateTask(id, taskDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Task updated successfully", task)
        );
    }

    // -------------------------------------------------------
    // PATCH /api/tasks/{id}/status — Status-only update
    // -------------------------------------------------------
    @PatchMapping("/{id:\\d+}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        log.info("Updating task {} status to: {}", id, status);
        TaskResponseDTO task = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(
                ApiResponse.success("Task status updated successfully", task)
        );
    }

    // -------------------------------------------------------
    // DELETE /api/tasks/{id} — Delete task
    // -------------------------------------------------------
    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        log.info("Deleting task: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(
                ApiResponse.success("Task deleted successfully")
        );
    }

    // -------------------------------------------------------
    // POST /api/tasks/filter — Filter with pagination
    // POST (not GET) because FilterDTO is sent as request body
    // -------------------------------------------------------
    @PostMapping("/filter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TaskResponseDTO>>> filterTasks(
            @RequestBody TaskFilterDTO filterDTO) {
        log.info("Filtering tasks with criteria: {}", filterDTO);
        Page<TaskResponseDTO> tasks = taskService.filterTasks(filterDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Tasks filtered successfully", tasks)
        );
    }
}