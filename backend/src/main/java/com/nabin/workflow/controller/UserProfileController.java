package com.nabin.workflow.controller;

import com.nabin.workflow.dto.common.ApiResponse;
import com.nabin.workflow.dto.request.ChangePasswordDTO;
import com.nabin.workflow.dto.request.UpdateProfileDTO;
import com.nabin.workflow.dto.response.UserProfileDTO;
import com.nabin.workflow.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserService userService;

    /**
     * Get current user's profile
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile() {
        log.info("Getting current user profile");

        UserProfileDTO profile = userService.getCurrentUserProfile();

        ApiResponse<UserProfileDTO> response = ApiResponse.success(
                "Profile retrieved successfully",
                profile
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update current user's profile
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO) {

        log.info("Updating current user profile");

        UserProfileDTO profile = userService.updateCurrentUserProfile(updateProfileDTO);

        ApiResponse<UserProfileDTO> response = ApiResponse.success(
                "Profile updated successfully",
                profile
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Change password (LOCAL users only)
     * PUT /api/users/me/password
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {

        log.info("Password change requested");

        userService.changePassword(changePasswordDTO);

        ApiResponse<Void> response = ApiResponse.success(
                "Password changed successfully. Please login again with your new password."
        );

        return ResponseEntity.ok(response);
    }
}
