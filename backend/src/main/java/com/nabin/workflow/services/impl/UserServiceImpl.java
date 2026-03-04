package com.nabin.workflow.services.impl;

import com.nabin.workflow.dto.request.ChangePasswordDTO;
import com.nabin.workflow.dto.request.UpdateProfileDTO;
import com.nabin.workflow.dto.response.RoleResponseDTO;
import com.nabin.workflow.dto.request.UserRegistrationDTO;
import com.nabin.workflow.dto.response.UserProfileDTO;
import com.nabin.workflow.dto.response.UserResponseDTO;
import com.nabin.workflow.entities.AuthProvider;
import com.nabin.workflow.entities.Role;
import com.nabin.workflow.entities.TaskStatus;
import com.nabin.workflow.entities.User;
import com.nabin.workflow.exception.DuplicateResourceException;
import com.nabin.workflow.exception.InvalidBusinessRuleException;
import com.nabin.workflow.exception.ResourceNotFoundException;
import com.nabin.workflow.exception.UnauthorizedException;
import com.nabin.workflow.mapper.DTOMapper;
import com.nabin.workflow.repository.*;
import com.nabin.workflow.util.SecurityUtil;
import com.nabin.workflow.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//Annotations
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    // Dependencies
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DTOMapper dtoMapper;
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    //  Method to SAVE user details
    @Override
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        log.info("Registering new user: {}", registrationDTO.getEmail());

        // Validate email not already taken
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", registrationDTO.getEmail());
            throw new DuplicateResourceException("Email", registrationDTO.getEmail());
        }

        // Validate username not already taken
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            log.warn("Registration failed: Username already exists - {}", registrationDTO.getUsername());
            throw new DuplicateResourceException("Username", registrationDTO.getUsername());
        }

        validateUserBusinessRules(registrationDTO);


        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    log.info("ROLE_USER not found, creating new role");
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });

        // Create user with ALL required fields
        User user = User.builder()
                .username(registrationDTO.getUsername())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .enabled(true)
                .provider(AuthProvider.LOCAL)      //  Required field!
                .providerId(null)                   // Null for local users
                .build();

        // Assign role
        user.setRoles(Set.of(userRole));

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully - ID: {}, Email: {}, Provider: {}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getProvider());

        return dtoMapper.toUserResponseDTO(savedUser);
    }
    // Method to GET user details - by id
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        // Fetch user or throw exception
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return dtoMapper.toUserResponseDTO(user);
    }


    //  Method to GET users - by email
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
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
                        .map(role -> new RoleResponseDTO(role.getId(), role.getName()))
                        .collect(Collectors.toSet()))
                .build();
    }

    private void validateUserBusinessRules(UserRegistrationDTO registrationDTO) {

        String[] reservedWords = {"admin", "root", "system", "administrator", "moderator"};
        String usernameLower = registrationDTO.getUsername().toLowerCase();

        for (String reserved : reservedWords) {
            if (usernameLower.contains(reserved)) {
                throw new InvalidBusinessRuleException(
                        "Username cannot contain reserved word: " + reserved
                );
            }
        }
    }
    /**
     * Get all users (Admin only)
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDTO> getAllUsers() {
        log.info("Getting all users");

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(dtoMapper::toUserResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete user (Admin only)
     */
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Attempting to delete user: {}", userId);

        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Prevent self-deletion
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Cannot delete your own account");
        }

        log.info("Deleting user: {} ({})", user.getEmail(), userId);

        try {
            // Delete in order (child entities first)

            // 1. Refresh tokens
            refreshTokenRepository.deleteByUser(user);
            log.debug("Deleted refresh tokens for user: {}", userId);

            // 2. Tasks (will cascade if properly configured, but explicit is better)
            taskRepository.deleteByUserId(userId);
            log.debug("Deleted tasks for user: {}", userId);

            // 3. Categories
            categoryRepository.deleteByUserId(userId);
            log.debug("Deleted categories for user: {}", userId);

            // 4. Clear role associations
            user.getRoles().clear();
            userRepository.saveAndFlush(user);
            log.debug("Cleared role associations for user: {}", userId);

            // 5. Delete user
            userRepository.delete(user);
            log.info("User deleted successfully: {}", userId);

        } catch (Exception e) {
            log.error("Error deleting user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
    /**
     * Get current user's profile with statistics
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public UserProfileDTO getCurrentUserProfile() {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Get user statistics
        long totalTasks = taskRepository.countByUserId(userId);
        long completedTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.DONE);
        long totalCategories = categoryRepository.countByUserId(userId);

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .totalCategories(totalCategories)
                .build();
    }

    /**
     * Update current user's profile
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public UserProfileDTO updateCurrentUserProfile(UpdateProfileDTO updateProfileDTO) {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        log.info("Updating profile for user: {}", user.getEmail());

        // Update username if provided and different
        if (updateProfileDTO.getUsername() != null
                && !updateProfileDTO.getUsername().equals(user.getUsername())) {

            if (userRepository.existsByUsername(updateProfileDTO.getUsername())) {
                throw new DuplicateResourceException("Username", updateProfileDTO.getUsername());
            }

            user.setUsername(updateProfileDTO.getUsername());
            log.info("Username updated to: {}", updateProfileDTO.getUsername());
        }

        // Update email if provided and different
        if (updateProfileDTO.getEmail() != null
                && !updateProfileDTO.getEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(updateProfileDTO.getEmail())) {
                throw new DuplicateResourceException("Email", updateProfileDTO.getEmail());
            }

            user.setEmail(updateProfileDTO.getEmail());
            log.info("Email updated to: {}", updateProfileDTO.getEmail());
        }

        userRepository.save(user);
        log.info("Profile updated successfully for user: {}", userId);

        return getCurrentUserProfile();
    }

    /**
     * Change password (LOCAL users only)
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if user is LOCAL (has password)
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new IllegalArgumentException(
                    "Cannot change password for OAuth users. " +
                            "You registered using " + user.getProvider() + " login."
            );
        }

        // Verify current password
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed: Incorrect current password for user {}", userId);
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Verify new password matches confirmation
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        // Verify new password is different from current
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

}
