package com.nabin.workflow.util;

import com.nabin.workflow.exception.UnauthorizedException;
import com.nabin.workflow.security.user.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    /**
     * Get currently authenticated user
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        throw new UnauthorizedException("Invalid authentication principal");
    }

    /**
     * Get current user's ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Get current user's email
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Get current user's username
     */
    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * Check if current user has a specific role
     */
    public static boolean hasRole(String role) {
        UserPrincipal user = getCurrentUser();
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * Check if user owns the resource
     */
    public static boolean isOwner(Long userId) {
        return getCurrentUserId().equals(userId);
    }

    /**
     * Validate user owns the resource, throw exception if not
     */
    public static void validateOwnership(Long userId) {
        if (!isOwner(userId)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
    }
}