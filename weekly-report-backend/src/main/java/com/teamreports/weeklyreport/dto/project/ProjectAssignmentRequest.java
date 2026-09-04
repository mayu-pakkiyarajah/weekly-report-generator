package com.teamreports.weeklyreport.dto.project;

import jakarta.validation.constraints.NotNull;

public record ProjectAssignmentRequest(@NotNull Long userId) {
}
