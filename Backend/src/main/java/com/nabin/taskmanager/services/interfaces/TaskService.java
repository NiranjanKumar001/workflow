package com.nabin.taskmanager.services.interfaces;

import com.nabin.taskmanager.dto.TaskRequestDTO;
import com.nabin.taskmanager.dto.TaskResponseDTO;
import com.nabin.taskmanager.entities.TaskStatus;

import java.util.List;

public interface TaskService {
    TaskResponseDTO createTask(Long userId, TaskRequestDTO taskRequestDTO);
    TaskResponseDTO getTaskById(Long userId, Long taskId);
    List<TaskResponseDTO> getAllTasksByUserId(Long userId);
    List<TaskResponseDTO> getTasksByStatus(Long userId, TaskStatus status);
    TaskResponseDTO updateTask(Long userId, Long taskId, TaskRequestDTO taskRequestDTO);
    void deleteTask(Long userId, Long taskId);
}