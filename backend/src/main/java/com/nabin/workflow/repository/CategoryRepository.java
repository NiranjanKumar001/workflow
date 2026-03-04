package com.nabin.workflow.repository;

import com.nabin.workflow.entities.Category;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long>
{
    List<Category> findByUserId(Long userId);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Category c WHERE c.user.id = :userId")
    void deleteByUserId(Long userId);
}
