package com.nabin.taskmanager.services.interfaces;

import com.nabin.taskmanager.dto.UserRegistrationDTO;
import com.nabin.taskmanager.dto.UserResponseDTO;
import jakarta.validation.Valid;

public interface UserService {
    UserResponseDTO registerUser(UserRegistrationDTO registrationDTO);
    UserResponseDTO getUserById(Long id);
    UserResponseDTO getUserByEmail(String email);
    UserResponseDTO getUserByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    UserResponseDTO updateUser(Long userId, @Valid UserRegistrationDTO updateDTO);
}