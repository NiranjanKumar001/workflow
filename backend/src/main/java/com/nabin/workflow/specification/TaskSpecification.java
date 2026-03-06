package com.nabin.workflow.specification;

import com.nabin.workflow.entities.Task;
import com.nabin.workflow.entities.TaskPriority;
import com.nabin.workflow.entities.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskSpecification {

    // ----------------------------------------------------------------
    // SINGLE-PURPOSE SPECIFICATIONS (used in TaskServiceImpl)
    // ----------------------------------------------------------------

    public static Specification<Task> hasUserId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) ->
                cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasCategoryId(Long categoryId) {
        return (root, query, cb) ->
                cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Overdue = dueDate is before today AND status is not COMPLETED or ARCHIVED
     * FIX: uses LocalDate, not LocalDateTime
     */
    public static Specification<Task> isOverdue() {
        return (root, query, cb) -> cb.and(
                cb.lessThan(root.get("dueDate"), LocalDate.now()),
                cb.notEqual(root.get("status"), TaskStatus.COMPLETED),
                cb.notEqual(root.get("status"), TaskStatus.ARCHIVED)
        );
    }

    public static Specification<Task> dueDateAfter(LocalDate from) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("dueDate"), from);
    }

    public static Specification<Task> dueDateBefore(LocalDate to) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("dueDate"), to);
    }

    public static Specification<Task> dueDateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) ->
                cb.between(root.get("dueDate"), from, to);
    }

    public static Specification<Task> searchByTitleOrDescription(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Excludes tasks whose status is any of the provided values.
     * Varargs so you can pass as many statuses as needed.
     */
    public static Specification<Task> statusNotIn(TaskStatus... statuses) {
        return (root, query, cb) ->
                cb.not(root.get("status").in(Arrays.asList(statuses)));
    }

    // ----------------------------------------------------------------
    // COMPOSITE SPECIFICATION (kept for backward-compat if needed)
    // FIX: dueDateFrom/To changed from LocalDateTime to LocalDate
    // ----------------------------------------------------------------

    public static Specification<Task> filterTasks(
            Long userId,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            String searchKeyword) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (dueDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }
            if (dueDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }
            if (searchKeyword != null && !searchKeyword.isBlank()) {
                String pattern = "%" + searchKeyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * FIX: uses LocalDate, not LocalDateTime
     */
    public static Specification<Task> overdueTasks(Long userId) {
        return Specification
                .where(hasUserId(userId))
                .and(isOverdue());
    }

    /**
     * FIX: uses LocalDate, not LocalDateTime
     */
    public static Specification<Task> tasksDueSoon(Long userId, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);

        return Specification
                .where(hasUserId(userId))
                .and(dueDateBetween(today, futureDate))
                .and(statusNotIn(TaskStatus.COMPLETED, TaskStatus.ARCHIVED));
    }
}