package com.nabin.workflow.services.impl;

import com.nabin.workflow.dto.request.TaskRequestDTO;
import com.nabin.workflow.dto.response.TaskResponseDTO;
import com.nabin.workflow.entities.Task;
import com.nabin.workflow.entities.TaskPriority;
import com.nabin.workflow.entities.TaskStatus;
import com.nabin.workflow.entities.User;
import com.nabin.workflow.exception.ResourceNotFoundException;
import com.nabin.workflow.exception.UnauthorizedException;
import com.nabin.workflow.mapper.DTOMapper;
import com.nabin.workflow.repository.TaskRepository;
import com.nabin.workflow.repository.UserRepository;
import com.nabin.workflow.util.SecurityUtil;
import com.nabin.workflow.services.interfaces.TaskService;
import com.nabin.workflow.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final DTOMapper dtoMapper;

    /**
     * Create a new task
     * Validates user owns the task
     */
    @Override
    @PreAuthorize("isAuthenticated()")  // Must be logged in
    public TaskResponseDTO createTask(Long userId, TaskRequestDTO taskRequestDTO) {
        // Validate ownership
        validateUserOwnership(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        validateTaskBusinessRules(taskRequestDTO);

        Task task = Task.builder()
                .title(taskRequestDTO.getTitle())
                .description(taskRequestDTO.getDescription())
                .status(taskRequestDTO.getStatus())
                .priority(taskRequestDTO.getPriority())
                .dueDate(taskRequestDTO.getDueDate())
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("Task created: {} by user: {}", savedTask.getId(), userId);

        return dtoMapper.toTaskResponseDTO(savedTask);
    }

    /**
     * Get task by ID
     * Validates user owns the task
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public TaskResponseDTO getTaskById(Long userId, Long taskId) {
        // Validate ownership
        validateUserOwnership(userId);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        return dtoMapper.toTaskResponseDTO(task);
    }

    /**
     * Get all tasks for user
     * Validates user owns the tasks
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDTO> getAllTasksByUserId(Long userId) {
        // Validate ownership
        validateUserOwnership(userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        List<Task> tasks = taskRepository.findByUserId(userId);

        return tasks.stream()
                .map(dtoMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by status
     * Validates user owns the tasks
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDTO> getTasksByStatus(Long userId, TaskStatus status) {
        // Validate ownership
        validateUserOwnership(userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        List<Task> tasks = taskRepository.findByUserIdAndStatus(userId, status);

        return tasks.stream()
                .map(dtoMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update task
     * Validates user owns the task
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public TaskResponseDTO updateTask(Long userId, Long taskId, TaskRequestDTO taskRequestDTO) {
        // Validate ownership
        validateUserOwnership(userId);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        validateTaskBusinessRules(taskRequestDTO);
        validateTaskStatusTransition(task.getStatus(), taskRequestDTO.getStatus());

        task.setTitle(taskRequestDTO.getTitle());
        task.setDescription(taskRequestDTO.getDescription());
        task.setStatus(taskRequestDTO.getStatus());
        task.setPriority(taskRequestDTO.getPriority());
        task.setDueDate(taskRequestDTO.getDueDate());

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated: {} by user: {}", updatedTask.getId(), userId);

        return dtoMapper.toTaskResponseDTO(updatedTask);
    }

    /**
     * Delete task
     * Validates user owns the task
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteTask(Long userId, Long taskId) {
        // Validate ownership
        validateUserOwnership(userId);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        taskRepository.delete(task);
        log.info("Task deleted: {} by user: {}", taskId, userId);
    }

    /**
     * Filter tasks with pagination
     * Validates user owns the tasks
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<TaskResponseDTO> getTasksWithFilters(
            Long userId,
            TaskStatus status,
            TaskPriority priority,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String searchKeyword,
            Pageable pageable) {

        // Validate ownership
        validateUserOwnership(userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Specification<Task> spec = TaskSpecification.filterTasks(
                userId, status, priority, dueDateFrom, dueDateTo, searchKeyword
        );

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        return taskPage.map(dtoMapper::toTaskResponseDTO);
    }

    /**
     * Get overdue tasks
     * Validates user owns the tasks
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDTO> getOverdueTasks(Long userId) {
        // Validate ownership
        validateUserOwnership(userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Specification<Task> spec = TaskSpecification.overdueTasks(userId);
        List<Task> tasks = taskRepository.findAll(spec);

        return tasks.stream()
                .map(dtoMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks due soon
     * Validates user owns the tasks
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDTO> getTasksDueSoon(Long userId, int daysAhead) {
        // Validate ownership
        validateUserOwnership(userId);

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
    // SECURITY VALIDATION METHODS
    // ========================================

    /**
     * Validate that the authenticated user matches the userId parameter
     * Throws UnauthorizedException if user tries to access another user's data
     */
    private void validateUserOwnership(Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        if (!currentUserId.equals(userId)) {
            log.warn("User {} attempted to access data for user {}", currentUserId, userId);
            throw new UnauthorizedException("You don't have permission to access this user's data");
        }
    }

    // ========================================
    // BUSINESS RULE VALIDATION METHODS
    // ========================================

    private void validateTaskBusinessRules(TaskRequestDTO taskRequestDTO) {
        if (taskRequestDTO.getTitle() != null && taskRequestDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty or just whitespace");
        }

        if (taskRequestDTO.getDueDate() != null) {
            if (taskRequestDTO.getDueDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Due date cannot be in the past");
            }

            if (taskRequestDTO.getDueDate().isAfter(LocalDateTime.now().plusYears(5))) {
                throw new IllegalArgumentException("Due date cannot be more than 5 years in the future");
            }
        }

        if (taskRequestDTO.getPriority() == TaskPriority.HIGH && taskRequestDTO.getDueDate() == null) {
            throw new IllegalArgumentException("HIGH priority tasks must have a due date");
        }

        if (taskRequestDTO.getDescription() != null &&
                taskRequestDTO.getDescription().length() > 2000) {
            throw new IllegalArgumentException("Description cannot exceed 2000 characters");
        }
    }

    private void validateTaskStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        if (currentStatus == TaskStatus.DONE && newStatus == TaskStatus.TODO) {
            throw new IllegalArgumentException(
                    "Cannot move task from DONE directly to TODO. Move to IN_PROGRESS first."
            );
        }
    }
}