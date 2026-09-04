package com.teamreports.weeklyreport.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AchievementDto(
        Long id,
        @NotBlank @Size(max = 1000) String description,
        boolean keyAchievement
) {
}
