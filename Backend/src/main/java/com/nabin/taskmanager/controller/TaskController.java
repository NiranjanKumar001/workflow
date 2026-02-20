package com.nabin.taskmanager.controller;

import com.nabin.taskmanager.dto.TaskRequestDTO;
import com.nabin.taskmanager.dto.TaskResponseDTO;
import com.nabin.taskmanager.entities.TaskStatus;
import com.nabin.taskmanager.services.interfaces.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // GET /api/tasks?userId=1
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(@RequestParam Long userId) {
        return ResponseEntity.ok(taskService.getAllTasksByUserId(userId));
    }

    // GET /api/tasks/1/2
    @GetMapping("/{userId}/{taskId}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @PathVariable Long userId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(userId, taskId));
    }

    // GET /api/tasks/1/status?status=TODO
    @GetMapping("/{userId}/status")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByStatus(
            @PathVariable Long userId,
            @RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(userId, status));
    }

    // POST /api/tasks?userId=1
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(
            @RequestParam Long userId,
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(userId, taskRequestDTO));
    }

    // PUT /api/tasks/1/2
    @PutMapping("/{userId}/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        return ResponseEntity.ok(taskService.updateTask(userId, taskId, taskRequestDTO));
    }

    // DELETE /api/tasks/1/2
    @DeleteMapping("/{userId}/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {
        taskService.deleteTask(userId, taskId);
        return ResponseEntity.noContent().build();
    }
}