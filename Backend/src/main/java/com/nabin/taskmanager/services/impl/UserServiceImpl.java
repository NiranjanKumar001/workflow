package com.nabin.taskmanager.services.impl;

import com.nabin.taskmanager.dto.RoleDTO;
import com.nabin.taskmanager.dto.UserRegistrationDTO;
import com.nabin.taskmanager.dto.UserResponseDTO;
import com.nabin.taskmanager.entities.Role;
import com.nabin.taskmanager.entities.User;
import com.nabin.taskmanager.exception.DuplicateResourceException;
import com.nabin.taskmanager.exception.ResourceNotFoundException;
import com.nabin.taskmanager.mapper.DTOMapper;
import com.nabin.taskmanager.repository.RoleRepository;
import com.nabin.taskmanager.repository.UserRepository;
import com.nabin.taskmanager.services.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//Annotations
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService
{
    // Dependencies
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DTOMapper dtoMapper;

//  Method to SAVE user details
    @Override
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {

        // Check duplicate email
        if(userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new DuplicateResourceException("user","email",registrationDTO.getEmail());
        }

        // Check duplicate username
        if(userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new DuplicateResourceException("user","username",registrationDTO.getUsername());
        }

        // Create new user and encode password
        User user = User.builder()
                .username(registrationDTO.getUsername())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .enabled(true)
                .build();

        // Get default role or create if missing
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(()-> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });
        // Assign role to user
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // Save user and
        User saveduser = userRepository.save(user);

        //return response
        return dtoMapper.toUserResponseDTO(saveduser);
    }

// Method to GET user details - by id
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id)
    {
        // Fetch user or throw exception
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return dtoMapper.toUserResponseDTO(user);
    }


//  Method to GET users - by email
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email)
    {
        // Fetch user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return dtoMapper.toUserResponseDTO(user);
    }

//  Methods to GET user - by Username
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return dtoMapper.toUserResponseDTO(user);
    }

//  Methods to CHECK user - by email
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

// Methods to CHECK user - by Username
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public UserResponseDTO updateUser(Long userId, @Valid UserRegistrationDTO updateDTO) {
        // Fetch user entity by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check for unique username
        if (!user.getUsername().equals(updateDTO.getUsername()) &&
                userRepository.existsByUsername(updateDTO.getUsername())) {
            throw new RuntimeException("Username already in use");
        }

        // Check for unique email
        if (!user.getEmail().equals(updateDTO.getEmail()) &&
                userRepository.existsByEmail(updateDTO.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Update fields
        user.setUsername(updateDTO.getUsername());
        user.setEmail(updateDTO.getEmail());

        // Optional: update password if present
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        // Save updated entity
        userRepository.save(user);

        // Map entity → UserResponseDTO
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream()
                        .map(role -> new RoleDTO(role.getId(), role.getName()))
                        .collect(Collectors.toSet()))
                .build();
    }
}
