package com.teamreports.weeklyreport.dto.user;

import com.teamreports.weeklyreport.entity.enums.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        boolean active,
        Instant createdAt
) {
}
