package com.nabin.taskmanager.services.impl;

import com.nabin.taskmanager.dto.TaskRequestDTO;
import com.nabin.taskmanager.dto.TaskResponseDTO;
import com.nabin.taskmanager.entities.Task;
import com.nabin.taskmanager.entities.TaskPriority;
import com.nabin.taskmanager.entities.TaskStatus;
import com.nabin.taskmanager.entities.User;
import com.nabin.taskmanager.exception.ResourceNotFoundException;
import com.nabin.taskmanager.mapper.DTOMapper;
import com.nabin.taskmanager.repository.TaskRepository;
import com.nabin.taskmanager.repository.UserRepository;
import com.nabin.taskmanager.services.interfaces.TaskService;
import com.nabin.taskmanager.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    // Dependencies
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final DTOMapper dtoMapper;

    @Override
    public TaskResponseDTO createTask(Long userId, TaskRequestDTO taskRequestDTO) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Business rule validation
        validateTaskBusinessRules(taskRequestDTO);  // Fixed typo

        // Create task
        Task task = Task.builder()
                .title(taskRequestDTO.getTitle())
                .description(taskRequestDTO.getDescription())
                .status(taskRequestDTO.getStatus())
                .priority(taskRequestDTO.getPriority())
                .dueDate(taskRequestDTO.getDueDate())
                .user(user)
                .build();

        // Save task
        Task savedTask = taskRepository.save(task);

        // Convert to DTO and return
        return dtoMapper.toTaskResponseDTO(savedTask);
    }

    // Get Task by ID from the user
    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        return dtoMapper.toTaskResponseDTO(task);
    }

    // Get All Task List by user ID
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasksByUserId(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        List<Task> tasks = taskRepository.findByUserId(userId);

        return tasks.stream()
                .map(dtoMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    // Get Task by status and user ID
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByStatus(Long userId, TaskStatus status) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        List<Task> tasks = taskRepository.findByUserIdAndStatus(userId, status);

        return tasks.stream()
                .map(dtoMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    // Update the task
    @Override
    public TaskResponseDTO updateTask(Long userId, Long taskId, TaskRequestDTO taskRequestDTO) {
        // Find task and verify ownership
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        //  Business rule validation
        validateTaskBusinessRules(taskRequestDTO);
        validateTaskStatusTransition(task.getStatus(), taskRequestDTO.getStatus());

        // Update task fields
        task.setTitle(taskRequestDTO.getTitle());
        task.setDescription(taskRequestDTO.getDescription());
        task.setStatus(taskRequestDTO.getStatus());
        task.setPriority(taskRequestDTO.getPriority());
        task.setDueDate(taskRequestDTO.getDueDate());

        // Save updated task
        Task updatedTask = taskRepository.save(task);

        // Convert to DTO and return
        return dtoMapper.toTaskResponseDTO(updatedTask);
    }

    @Override
    public void deleteTask(Long userId, Long taskId) {
        // Find task and verify ownership
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Delete task
        taskRepository.delete(task);
    }

    // Advanced filtering with pagination
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getTasksWithFilters(
            Long userId,
            TaskStatus status,
            TaskPriority priority,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String searchKeyword,
            Pageable pageable) {

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        // Build specification
        Specification<Task> spec = TaskSpecification.filterTasks(
                userId, status, priority, dueDateFrom, dueDateTo, searchKeyword
        );

        // Execute query with pagination
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        // Convert to DTO
        return taskPage.map(dtoMapper::toTaskResponseDTO);
    }

    // Get overdue tasks
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getOverdueTasks(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Specification<Task> spec = TaskSpecification.overdueTasks(userId);
        List<Task> tasks = taskRepository.findAll(spec);

        return tasks.stream()
                .map(dtoMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    // Get tasks due soon
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksDueSoon(Long userId, int daysAhead) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Specification<Task> spec = TaskSpecification.tasksDueSoon(userId, daysAhead);
        List<Task> tasks = taskRepository.findAll(spec);

        return tasks.stream()
                .map(dtoMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    // ========================================
    // BUSINESS RULE VALIDATION METHODS
    // ========================================

    /**
     * Validate task business rules
     */
    private void validateTaskBusinessRules(TaskRequestDTO taskRequestDTO) {
        // Rule 1: Title should not be just whitespace
        if (taskRequestDTO.getTitle() != null && taskRequestDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty or just whitespace");
        }

        // Rule 2: Due date validation (if provided)
        if (taskRequestDTO.getDueDate() != null) {
            // Cannot be in the past
            if (taskRequestDTO.getDueDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Due date cannot be in the past");
            }

            // Cannot be too far in future (max 5 years)
            if (taskRequestDTO.getDueDate().isAfter(LocalDateTime.now().plusYears(5))) {
                throw new IllegalArgumentException("Due date cannot be more than 5 years in the future");
            }
        }

        // Rule 3: HIGH priority tasks should have a due date
        if (taskRequestDTO.getPriority() == TaskPriority.HIGH && taskRequestDTO.getDueDate() == null) {
            throw new IllegalArgumentException("HIGH priority tasks must have a due date");
        }

        // Rule 4: Description length validation (if provided)
        if (taskRequestDTO.getDescription() != null &&
                taskRequestDTO.getDescription().length() > 2000) {
            throw new IllegalArgumentException("Description cannot exceed 2000 characters");
        }
    }

    /**
     * Validate task status transitions
     */
    private void validateTaskStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        // Rule 1: Cannot move from DONE back to TODO directly
        if (currentStatus == TaskStatus.DONE && newStatus == TaskStatus.TODO) {
            throw new IllegalArgumentException(
                    "Cannot move task from DONE directly to TODO. Move to IN_PROGRESS first."
            );
        }

        // Rule 2: (Optional) Add more status transition rules if needed
        // For example: Cannot skip IN_PROGRESS when moving from TODO to DONE
        /*
        if (currentStatus == TaskStatus.TODO && newStatus == TaskStatus.DONE) {
            throw new IllegalArgumentException(
                "Cannot move task from TODO directly to DONE. Move to IN_PROGRESS first."
            );
        }
        */
    }
}