package com.nabin.workflow.services.impl;

import com.nabin.workflow.dto.request.TaskRequestDTO;
import com.nabin.workflow.dto.request.TaskFilterDTO;
import com.nabin.workflow.dto.request.TaskUpdateDTO;
import com.nabin.workflow.dto.response.TaskResponseDTO;
import com.nabin.workflow.dto.response.TaskStatsDTO;
import com.nabin.workflow.entities.*;
import com.nabin.workflow.exception.InvalidBusinessRuleException;
import com.nabin.workflow.exception.ResourceNotFoundException;
import com.nabin.workflow.mapper.DTOMapper;
import com.nabin.workflow.repository.CategoryRepository;
import com.nabin.workflow.repository.TaskRepository;
import com.nabin.workflow.repository.UserRepository;
import com.nabin.workflow.services.interfaces.TaskService;
import com.nabin.workflow.specification.TaskSpecification;
import com.nabin.workflow.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final CategoryRepository categoryRepository;
    private final DTOMapper dtoMapper;

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    @PreAuthorize("isAuthenticated()")
    public TaskResponseDTO createTask(TaskRequestDTO taskDTO) {
        Long userId = SecurityUtil.getCurrentUserId();

        // FIX #4: Existence check before any other logic
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // FIX #1: LocalDate comparison (was LocalDateTime)
        validateDueDateForCreate(taskDTO.getDueDate());

        // FIX #9: HIGH/URGENT priority requires a due date
        validatePriorityRequiresDueDate(taskDTO.getPriority(), taskDTO.getDueDate());

        Task task = Task.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .status(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.TODO)
                .priority(taskDTO.getPriority() != null ? taskDTO.getPriority() : TaskPriority.MEDIUM)
                .dueDate(taskDTO.getDueDate())
                .user(user)
                .build();

        if (taskDTO.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(taskDTO.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", taskDTO.getCategoryId()));
            task.setCategory(category);
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task created: {} by user: {}", savedTask.getId(), userId);
        return dtoMapper.toTaskResponseDTO(savedTask);
    }

    // =========================================================
    // READ
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public TaskResponseDTO getTaskById(Long taskId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return dtoMapper.toTaskResponseDTO(task);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDTO> getAllTasksForCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return taskRepository.findByUserId(userId).stream()
                .map(dtoMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDTO> getTasksByStatus(TaskStatus status) {
        Long userId = SecurityUtil.getCurrentUserId();
        return taskRepository.findByUserIdAndStatus(userId, status).stream()
                .map(dtoMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    @PreAuthorize("isAuthenticated()")
    // FIX #7: Uses TaskUpdateDTO (all fields optional), not TaskCreateDTO
    public TaskResponseDTO updateTask(Long taskId, TaskUpdateDTO taskDTO) {
        Long userId = SecurityUtil.getCurrentUserId();

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (taskDTO.getTitle() != null) {
            if (taskDTO.getTitle().isBlank()) {
                throw new InvalidBusinessRuleException("Task title cannot be empty or just whitespace");
            }
            task.setTitle(taskDTO.getTitle());
        }

        if (taskDTO.getDescription() != null) {
            if (taskDTO.getDescription().length() > 5000) {
                throw new InvalidBusinessRuleException("Description cannot exceed 5000 characters");
            }
            task.setDescription(taskDTO.getDescription());
        }

        if (taskDTO.getPriority() != null) {
            task.setPriority(taskDTO.getPriority());
        }

        // FIX #3: Due date validation only when a NEW due date is explicitly provided
        if (taskDTO.getDueDate() != null) {
            validateDueDateForCreate(taskDTO.getDueDate());
            task.setDueDate(taskDTO.getDueDate());
        }

        // FIX #9: After applying priority/dueDate changes, check HIGH priority rule
        // using the task's resolved values (existing + any updates)
        LocalDate resolvedDueDate = taskDTO.getDueDate() != null ? taskDTO.getDueDate() : task.getDueDate();
        TaskPriority resolvedPriority = taskDTO.getPriority() != null ? taskDTO.getPriority() : task.getPriority();
        validatePriorityRequiresDueDate(resolvedPriority, resolvedDueDate);

        // FIX #5: Full transition matrix via canTransitionTo()
        if (taskDTO.getStatus() != null && taskDTO.getStatus() != task.getStatus()) {
            updateTaskStatusInternal(task, taskDTO.getStatus());
        }

        if (taskDTO.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(taskDTO.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", taskDTO.getCategoryId()));
            task.setCategory(category);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated: {} by user: {}", taskId, userId);
        return dtoMapper.toTaskResponseDTO(updatedTask);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatus newStatus) {
        Long userId = SecurityUtil.getCurrentUserId();

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        updateTaskStatusInternal(task, newStatus);

        Task updatedTask = taskRepository.save(task);
        log.info("Task status updated: {} → {} by user: {}", taskId, newStatus, userId);
        return dtoMapper.toTaskResponseDTO(updatedTask);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteTask(Long taskId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        taskRepository.delete(task);
        log.info("Task deleted: {} by user: {}", taskId, userId);
    }

    // =========================================================
    // FILTERING & ADVANCED QUERIES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    // FIX #8: Uses TaskFilterDTO with LocalDate fields — no more LocalDateTime mismatch
    public Page<TaskResponseDTO> filterTasks(TaskFilterDTO filterDTO) {
        Long userId = SecurityUtil.getCurrentUserId();

        Specification<Task> spec = Specification.where(TaskSpecification.hasUserId(userId));

        if (filterDTO.getStatus() != null) {
            spec = spec.and(TaskSpecification.hasStatus(filterDTO.getStatus()));
        }
        if (filterDTO.getPriority() != null) {
            spec = spec.and(TaskSpecification.hasPriority(filterDTO.getPriority()));
        }
        if (filterDTO.getCategoryId() != null) {
            spec = spec.and(TaskSpecification.hasCategoryId(filterDTO.getCategoryId()));
        }
        if (Boolean.TRUE.equals(filterDTO.getOverdue())) {
            spec = spec.and(TaskSpecification.isOverdue());
        }
        if (filterDTO.getDueDateFrom() != null) {
            spec = spec.and(TaskSpecification.dueDateAfter(filterDTO.getDueDateFrom()));
        }
        if (filterDTO.getDueDateTo() != null) {
            spec = spec.and(TaskSpecification.dueDateBefore(filterDTO.getDueDateTo()));
        }
        if (filterDTO.getSearchQuery() != null && !filterDTO.getSearchQuery().isBlank()) {
            spec = spec.and(TaskSpecification.searchByTitleOrDescription(filterDTO.getSearchQuery()));
        }

        String sortBy = (filterDTO.getSortBy() != null && !filterDTO.getSortBy().isBlank())
                ? filterDTO.getSortBy() : "createdAt";

        String sortDirection = (filterDTO.getSortDirection() != null && !filterDTO.getSortDirection().isBlank())
                ? filterDTO.getSortDirection() : "DESC";

        int page = filterDTO.getPage() != null ? filterDTO.getPage() : 0;
        int size = filterDTO.getSize() != null ? filterDTO.getSize() : 10;

        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        return taskRepository.findAll(spec, pageable).map(dtoMapper::toTaskResponseDTO);

    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDTO> getOverdueTasks() {
        Long userId = SecurityUtil.getCurrentUserId();
        return taskRepository.findAll(
                Specification.where(TaskSpecification.hasUserId(userId))
                        .and(TaskSpecification.isOverdue())
        ).stream().map(dtoMapper::toTaskResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDTO> getTasksDueSoon(int days) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);

        return taskRepository.findAll(
                Specification.where(TaskSpecification.hasUserId(userId))
                        .and(TaskSpecification.dueDateBetween(today, futureDate))
                        .and(TaskSpecification.statusNotIn(TaskStatus.COMPLETED, TaskStatus.ARCHIVED))
        ).stream().map(dtoMapper::toTaskResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Long countOverdueTasks() {
        Long userId = SecurityUtil.getCurrentUserId();
        return taskRepository.count(
                Specification.where(TaskSpecification.hasUserId(userId))
                        .and(TaskSpecification.isOverdue())
        );
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    /**
     * FIX #5: Full transition validation via canTransitionTo() on entity.
     * Also sets/clears completedAt timestamp automatically.
     */
    private void updateTaskStatusInternal(Task task, TaskStatus newStatus) {
        if (!task.canTransitionTo(newStatus)) {
            throw new InvalidBusinessRuleException(
                    "Invalid status transition: cannot move from " + task.getStatus() + " to " + newStatus +
                            ". Valid transitions: " + java.util.Arrays.toString(task.getStatus().getValidTransitions())
            );
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);

        if (newStatus == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (newStatus != TaskStatus.COMPLETED) {
            task.setCompletedAt(null);
        }
    }

    /**
     * FIX #1: Due date validation uses LocalDate.now(), not LocalDateTime.
     * FIX #3: Only called during CREATE — not blindly during updates.
     */
    private void validateDueDateForCreate(LocalDate dueDate) {
        if (dueDate == null) return;

        if (dueDate.isBefore(LocalDate.now())) {
            throw new InvalidBusinessRuleException("Due date cannot be in the past");
        }
        if (dueDate.isAfter(LocalDate.now().plusYears(5))) {
            throw new InvalidBusinessRuleException("Due date cannot be more than 5 years in the future");
        }
    }

    /**
     * FIX #9: HIGH/URGENT priority requires a due date.
     * During updates, resolvedDueDate is the task's effective due date (existing or newly set).
     */
    private void validatePriorityRequiresDueDate(TaskPriority priority, LocalDate resolvedDueDate) {
        if ((priority == TaskPriority.HIGH || priority == TaskPriority.URGENT)
                && resolvedDueDate == null) {
            throw new InvalidBusinessRuleException(
                    priority.getDisplayName() + " priority tasks must have a due date"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public TaskStatsDTO getTaskStats() {
        Long userId = SecurityUtil.getCurrentUserId();

        long total      = taskRepository.countByUserId(userId);
        long todo       = taskRepository.countByUserIdAndStatus(userId, TaskStatus.TODO);
        long inProgress = taskRepository.countByUserIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        long done       = taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED);
        long overdue    = taskRepository.count(
                Specification.where(TaskSpecification.hasUserId(userId))
                        .and(TaskSpecification.isOverdue())
        );

        return TaskStatsDTO.builder()
                .totalTasks(total)
                .todoTasks(todo)
                .inProgressTasks(inProgress)
                .doneTasks(done)
                .overdueTasks(overdue)
                .build();
    }
}