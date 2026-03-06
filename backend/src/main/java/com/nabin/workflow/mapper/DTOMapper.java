package com.nabin.workflow.mapper;

import com.nabin.workflow.dto.request.CategoryRequestDTO;
import com.nabin.workflow.dto.response.TaskResponseDTO;
import com.nabin.workflow.dto.response.UserResponseDTO;
import com.nabin.workflow.dto.response.CategoryResponseDTO;
import com.nabin.workflow.entities.Role;
import com.nabin.workflow.entities.Task;
import com.nabin.workflow.entities.User;
import com.nabin.workflow.entities.Category;
import org.springframework.stereotype.Component;
import com.nabin.workflow.dto.response.RoleResponseDTO;

import java.util.stream.Collectors;

@Component
public class DTOMapper {

    /**
     * Convert Task entity to TaskResponseDTO
     */
    public TaskResponseDTO toTaskResponseDTO(Task task) {
        if (task == null) {
            return null;
        }

        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .overdue(task.isOverdue())
                .userId(task.getUser().getId())
                .username(task.getUser().getUsername())
                .categoryId(task.getCategory() != null ? task.getCategory().getId() : null)
                .categoryName(task.getCategory() != null ? task.getCategory().getName() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }

    /**
     * Convert User entity to UserResponseDTO
     */
    public UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream()
                        .map(role -> RoleResponseDTO.builder()
                                .id(role.getId())
                                .name(role.getName())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }

    /**
     * Convert Category entity to CategoryResponseDTO
     */
    public CategoryResponseDTO toCategoryResponseDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .color(category.getColor())
                .userId(category.getUser().getId())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}