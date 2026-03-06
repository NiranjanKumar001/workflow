package com.nabin.workflow.services.impl;

import com.nabin.workflow.dto.request.ChangePasswordDTO;
import com.nabin.workflow.dto.request.ResetPasswordRequest;
import com.nabin.workflow.dto.request.UpdateProfileDTO;
import com.nabin.workflow.dto.request.UserRegistrationDTO;
import com.nabin.workflow.dto.response.UserProfileDTO;
import com.nabin.workflow.dto.response.UserResponseDTO;
import com.nabin.workflow.entities.*;
import com.nabin.workflow.exception.DuplicateResourceException;
import com.nabin.workflow.exception.InvalidBusinessRuleException;
import com.nabin.workflow.exception.ResourceNotFoundException;
import com.nabin.workflow.exception.UnauthorizedException;
import com.nabin.workflow.mapper.DTOMapper;
import com.nabin.workflow.repository.*;
import com.nabin.workflow.services.EmailService;
import com.nabin.workflow.util.SecurityUtil;
import com.nabin.workflow.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    // ============================================
    // DEPENDENCIES
    // ============================================
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DTOMapper dtoMapper;
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.verification.token-expiration}")
    private Long verificationTokenExpiration;

    @Value("${app.password-reset.token-expiration}")
    private Long passwordResetTokenExpiration;

    // ============================================
    // REGISTRATION & EMAIL VERIFICATION
    // ============================================

    /**
     * Register new user with email verification
     */
    @Override
    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        log.info("Registering new user: {}", registrationDTO.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", registrationDTO.getEmail());
            throw new DuplicateResourceException("Email", registrationDTO.getEmail());
        }

        // Check if username already exists
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            log.warn("Registration failed: Username already exists - {}", registrationDTO.getUsername());
            throw new DuplicateResourceException("Username", registrationDTO.getUsername());
        }

        // Validate business rules
        validateUserBusinessRules(registrationDTO);

        // Get or create ROLE_USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    log.info("ROLE_USER not found, creating new role");
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });

        // Create user (disabled until email verified)
        User user = User.builder()
                .username(registrationDTO.getUsername())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .enabled(false)  // ✅ Disabled until email verified
                .provider(AuthProvider.LOCAL)
                .providerId(null)
                .build();

        user.setRoles(Set.of(userRole));

        // Save user
        User savedUser = userRepository.save(user);

        // ✅ Generate and send verification email
        createVerificationToken(savedUser);

        log.info("✅ User registered successfully - ID: {}, Email: {}, Provider: {}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getProvider());

        return dtoMapper.toUserResponseDTO(savedUser);
    }

    /**
     * Create verification token and send email
     */
    private void createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(verificationTokenExpiration / 1000);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .verified(false)
                .build();

        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);

        log.info("✅ Verification token created and email sent to: {}", user.getEmail());
    }

    /**
     * Verify email
     */
    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        if (verificationToken.getVerified()) {
            throw new IllegalArgumentException("Email already verified");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setVerified(true);
        verificationTokenRepository.save(verificationToken);

        log.info("✅ Email verified successfully for user: {}", user.getEmail());
    }

    /**
     * Resend verification email
     */
    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.getEnabled()) {
            throw new IllegalArgumentException("Email already verified");
        }

        // Delete old token
        verificationTokenRepository.findByUser(user).ifPresent(verificationTokenRepository::delete);

        // Create new token
        createVerificationToken(user);

        log.info("✅ Verification email resent to: {}", email);
    }

    // ============================================
    // PASSWORD RESET
    // ============================================

    /**
     * Forgot password - send reset email
     */
    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Delete old reset tokens
        passwordResetTokenRepository.deleteByUser(user);

        // Generate token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(passwordResetTokenExpiration / 1000);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);

        log.info("✅ Password reset email sent to: {}", email);
    }

    /**
     * Reset password
     */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        if (resetToken.getUsed()) {
            throw new IllegalArgumentException("Reset token already used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("✅ Password reset successfully for user: {}", user.getEmail());
    }

    // ============================================
    // USER PROFILE MANAGEMENT
    // ============================================

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
        long completedTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED);
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
    @Transactional
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
        log.info("✅ Profile updated successfully for user: {}", userId);

        return getCurrentUserProfile();
    }

    /**
     * Change password (LOCAL users only)
     */
    @Override
    @Transactional
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

        log.info("✅ Password changed successfully for user: {}", userId);
    }

    // ============================================
    // USER CRUD OPERATIONS
    // ============================================

    /**
     * Get user by ID
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return dtoMapper.toUserResponseDTO(user);
    }

    /**
     * Get user by email
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return dtoMapper.toUserResponseDTO(user);
    }

    /**
     * Get user by username
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return dtoMapper.toUserResponseDTO(user);
    }

    /**
     * Check if email exists
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if username exists
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Update user
     */
    @Override
    @Transactional
    public UserResponseDTO updateUser(Long userId, UserRegistrationDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check for unique username
        if (!user.getUsername().equals(updateDTO.getUsername()) &&
                userRepository.existsByUsername(updateDTO.getUsername())) {
            throw new DuplicateResourceException("Username", updateDTO.getUsername());
        }

        // Check for unique email
        if (!user.getEmail().equals(updateDTO.getEmail()) &&
                userRepository.existsByEmail(updateDTO.getEmail())) {
            throw new DuplicateResourceException("Email", updateDTO.getEmail());
        }

        // Update fields
        user.setUsername(updateDTO.getUsername());
        user.setEmail(updateDTO.getEmail());

        // Update password if provided
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        userRepository.save(user);

        return dtoMapper.toUserResponseDTO(user);
    }

    // ============================================
    // ADMIN OPERATIONS
    // ============================================

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
    @PreAuthorize("hasRole('ADMIN')")
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

            // 2. Tasks
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

    // ============================================
    // VALIDATION & UTILITIES
    // ============================================

    /**
     * Validate user business rules
     */
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
     * Scheduled cleanup of expired tokens (Daily at 4 AM)
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired tokens...");

        verificationTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());

        log.info("Expired tokens cleaned up");
    }
}