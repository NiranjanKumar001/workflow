package com.nabin.taskmanager.services;

import com.nabin.taskmanager.dto.UserRegistrationDTO;
import com.nabin.taskmanager.dto.UserResponseDTO;
import com.nabin.taskmanager.entities.Role;
import com.nabin.taskmanager.entities.User;
import com.nabin.taskmanager.exception.DuplicateResourceException;
import com.nabin.taskmanager.exception.ResourceNotFoundException;
import com.nabin.taskmanager.mapper.DTOMapper;
import com.nabin.taskmanager.repository.RoleRepository;
import com.nabin.taskmanager.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        // Fetch user or throw exception
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return dtoMapper.toUserResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email)
    {
        // Fetch user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return dtoMapper.toUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO getUserByUsername(String username) {
        return null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }

    @Override
    public boolean existsByUsername(String username) {
        return false;
    }
}
