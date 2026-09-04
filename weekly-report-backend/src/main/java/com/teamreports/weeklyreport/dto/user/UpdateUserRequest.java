package com.teamreports.weeklyreport.dto.user;

import com.teamreports.weeklyreport.entity.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotNull Role role,
        @NotNull Boolean active
) {
}
