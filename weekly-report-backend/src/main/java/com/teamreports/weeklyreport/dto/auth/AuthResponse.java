package com.teamreports.weeklyreport.dto.auth;

import com.teamreports.weeklyreport.entity.enums.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String fullName,
        String email,
        Role role
) {
    public static AuthResponse of(String token, Long userId, String fullName, String email, Role role) {
        return new AuthResponse(token, "Bearer", userId, fullName, email, role);
    }
}
