package com.nabin.workflow.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    // FIX #1: Consistently LocalDate, not LocalDateTime
    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public boolean isOverdue() {
        if (dueDate == null || status == TaskStatus.COMPLETED || status == TaskStatus.ARCHIVED) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }

    // FIX #5: Full transition matrix used by service layer
    public boolean canTransitionTo(TaskStatus newStatus) {
        if (this.status == newStatus) return true;

        return switch (this.status) {
            case TODO       -> newStatus == TaskStatus.IN_PROGRESS || newStatus == TaskStatus.ARCHIVED;
            case IN_PROGRESS-> newStatus == TaskStatus.TODO || newStatus == TaskStatus.COMPLETED || newStatus == TaskStatus.ARCHIVED;
            case COMPLETED  -> newStatus == TaskStatus.TODO || newStatus == TaskStatus.ARCHIVED;
            case ARCHIVED   -> false;
        };
    }
}