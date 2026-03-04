package com.nabin.workflow.mapper;

import com.nabin.workflow.dto.request.CategoryRequestDTO;
import com.nabin.workflow.dto.response.RoleResponseDTO;
import com.nabin.workflow.dto.response.TaskResponseDTO;
import com.nabin.workflow.dto.response.UserResponseDTO;
import com.nabin.workflow.entities.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DTOMapper {

    // User Entity -> UserResponseDTO
    public UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // Task Entity -> TaskResponseDTO
    public TaskResponseDTO toTaskResponseDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .categories(task.getCategories() != null ?
                        task.getCategories().stream()
                                .map(this::toCategoryDTO)
                                .collect(Collectors.toSet()) : null)
                .build();
    }

    // Category Entity -> CategoryDTO
    public CategoryRequestDTO toCategoryDTO(Category category) {
        return CategoryRequestDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .build();
    }

    // Role Entity -> RoleDTO
    public RoleResponseDTO toRoleDTO(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}