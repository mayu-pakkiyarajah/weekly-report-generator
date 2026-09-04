package com.teamreports.weeklyreport.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Public self-registration always creates a TEAM_MEMBER account.
 * Promoting someone to MANAGER is an explicit admin action (see UserManagementController),
 * never something the registration endpoint can be tricked into doing.
 */
public record RegisterRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
