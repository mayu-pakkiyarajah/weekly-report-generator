package com.teamreports.weeklyreport.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlockerDto(
        Long id,
        @NotBlank @Size(max = 1000) String description,
        boolean keyIssue
) {
}
