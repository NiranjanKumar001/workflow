package com.nabin.taskmanager.services.interfaces;

import com.nabin.taskmanager.dto.TaskRequestDTO;
import com.nabin.taskmanager.dto.TaskResponseDTO;
import com.nabin.taskmanager.entities.TaskPriority;
import com.nabin.taskmanager.entities.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    // Methods
    TaskResponseDTO createTask(Long userId, TaskRequestDTO taskRequestDTO);
    TaskResponseDTO getTaskById(Long userId, Long taskId);
    List<TaskResponseDTO> getAllTasksByUserId(Long userId);
    List<TaskResponseDTO> getTasksByStatus(Long userId, TaskStatus status);
    TaskResponseDTO updateTask(Long userId, Long taskId, TaskRequestDTO taskRequestDTO);
    void deleteTask(Long userId, Long taskId);

    // Advanced filtering and pagination
    Page<TaskResponseDTO> getTasksWithFilters(
            Long userId,
            TaskStatus status,
            TaskPriority priority,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String searchKeyword,
            Pageable pageable
    );

    // Get overdue tasks
    List<TaskResponseDTO> getOverdueTasks(Long userId);

    // Get tasks due soon
    List<TaskResponseDTO> getTasksDueSoon(Long userId, int daysAhead);
}