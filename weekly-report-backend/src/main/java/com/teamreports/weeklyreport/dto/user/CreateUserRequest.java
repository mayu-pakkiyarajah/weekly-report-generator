package com.teamreports.weeklyreport.dto.user;

import com.teamreports.weeklyreport.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Used by a manager/admin to invite a new team member directly with an assigned role. */
public record CreateUserRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 8, max = 100) String temporaryPassword,
        @NotNull Role role
) {
}
