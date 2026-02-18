package com.nabin.taskmanager.services;

import com.nabin.taskmanager.dto.UserRegistrationDTO;
import com.nabin.taskmanager.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO registerUser(UserRegistrationDTO registrationDTO);
    UserResponseDTO getUserById(Long id);
    UserResponseDTO getUserByEmail(String email);
    UserResponseDTO getUserByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}