package com.teamreports.weeklyreport.dto.report;

import com.teamreports.weeklyreport.entity.enums.TaskType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record HoursEntryDto(
        @NotNull TaskType taskType,
        @DecimalMin("0.0") double hours
) {
}
