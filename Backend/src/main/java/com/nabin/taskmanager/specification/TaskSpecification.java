package com.nabin.taskmanager.specification;

import com.nabin.taskmanager.entities.Task;
import com.nabin.taskmanager.entities.TaskPriority;
import com.nabin.taskmanager.entities.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    /**
     * Build dynamic specification based on filter criteria
     */
    public static Specification<Task> filterTasks(
            Long userId,
            TaskStatus status,
            TaskPriority priority,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String searchKeyword) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by user ID (security requirement)
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by priority
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }

            // Filter by due date range
            if (dueDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }
            if (dueDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }

            // Search in title and description
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String likePattern = "%" + searchKeyword.toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), likePattern);
                Predicate descriptionMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), likePattern);
                predicates.add(criteriaBuilder.or(titleMatch, descriptionMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Get overdue tasks for a user
     */
    public static Specification<Task> overdueTasks(Long userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            predicates.add(criteriaBuilder.lessThan(root.get("dueDate"), LocalDateTime.now()));
            predicates.add(criteriaBuilder.notEqual(root.get("status"), TaskStatus.DONE));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Get tasks due soon (within next N days)
     */
    public static Specification<Task> tasksDueSoon(Long userId, int daysAhead) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime futureDate = now.plusDays(daysAhead);

            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            predicates.add(criteriaBuilder.between(root.get("dueDate"), now, futureDate));
            predicates.add(criteriaBuilder.notEqual(root.get("status"), TaskStatus.DONE));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}