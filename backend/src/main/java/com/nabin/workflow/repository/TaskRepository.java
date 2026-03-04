package com.nabin.workflow.repository;

import com.nabin.workflow.entities.Task;
import com.nabin.workflow.entities.TaskPriority;
import com.nabin.workflow.entities.TaskStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;  // ✅ ADD THIS
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task>
{

 //Methods
 List<Task> findByUserId(Long userId);
 List<Task> findByStatus(TaskStatus status);
 List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
 List<Task> findByUserIdAndPriority(Long userId, TaskPriority priority);
 Optional<Task> findByIdAndUserId(Long id, Long userId);

 @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.dueDate < :now AND t.status != 'DONE'")
 List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);
 long countByUserId(Long userId);
 long countByUserIdAndStatus(Long userId, TaskStatus status);

 @Modifying
 @Transactional
 @Query("DELETE FROM Task t WHERE t.user.id = :userId")
 void deleteByUserId(Long userId);
}