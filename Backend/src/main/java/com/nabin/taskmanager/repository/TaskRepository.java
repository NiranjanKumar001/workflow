package com.nabin.taskmanager.repository;

import com.nabin.taskmanager.entities.Task;
import com.nabin.taskmanager.entities.TaskPriority;
import com.nabin.taskmanager.entities.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long>
{
   List<Task> findByUserId(Long id);
    List<Task> findByStatus(TaskStatus status);

    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
    List<Task> findByUserIdAndPriority(Long userId, TaskPriority priority);

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    // Find overdue tasks
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.dueDate < :now AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Count tasks by status for a user
    long countByUserIdAndStatus(Long userId, TaskStatus status);
}
