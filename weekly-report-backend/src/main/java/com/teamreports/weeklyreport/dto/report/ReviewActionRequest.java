package com.teamreports.weeklyreport.dto.report;

import com.teamreports.weeklyreport.entity.enums.ReviewAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewActionRequest(
        @NotNull ReviewAction action,
        @NotBlank @Size(max = 2000) String comment
) {
}
