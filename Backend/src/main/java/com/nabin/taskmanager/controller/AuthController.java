package com.nabin.taskmanager.controller;

import com.nabin.taskmanager.dto.LoginResponseDTO;
import com.nabin.taskmanager.dto.UserLoginDTO;
import com.nabin.taskmanager.dto.UserRegistrationDTO;
import com.nabin.taskmanager.dto.UserResponseDTO;
import com.nabin.taskmanager.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Register a new user
     * POST /api/auth/register
     *
     * @param registrationDTO User registration data
     * @return Created user details (without password)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRegistrationDTO registrationDTO) {

        UserResponseDTO user = userService.registerUser(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Login endpoint (placeholder for now - will add JWT later)
     * POST /api/auth/login
     *
     * @param loginDTO User login credentials
     * @return Login response with token (placeholder for now)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody UserLoginDTO loginDTO) {

        // For now, just verify user exists
        UserResponseDTO user = userService.getUserByEmail(loginDTO.getEmail());

        // TODO: Add password verification and JWT generation later
        LoginResponseDTO response = LoginResponseDTO.builder()
                .token("temporary-token-will-be-replaced-with-jwt")
                .type("Bearer")
                .user(user)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Check if email exists
     * GET /api/auth/check-email?email=test@example.com
     *
     * @param email Email to check
     * @return True if email exists, false otherwise
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * Check if username exists
     * GET /api/auth/check-username?username=test-user
     *
     * @param username Username to check
     * @return True if username exists, false otherwise
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    /**
     * Test endpoint to verify auth controller is working
     * GET /api/auth/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth Controller is working!");
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserRegistrationDTO updateDTO) {

        UserResponseDTO updatedUser = userService.updateUser(userId, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }
}