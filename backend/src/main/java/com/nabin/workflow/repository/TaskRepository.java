package com.nabin.workflow.repository;

import com.nabin.workflow.entities.Task;
import com.nabin.workflow.entities.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

 List<Task> findByUserId(Long userId);

 List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);

 Optional<Task> findByIdAndUserId(Long id, Long userId);

 // Count methods for statistics
 long countByUserId(Long userId);

 long countByUserIdAndStatus(Long userId, TaskStatus status);

 // Delete method for admin
 void deleteByUserId(Long userId);

}
