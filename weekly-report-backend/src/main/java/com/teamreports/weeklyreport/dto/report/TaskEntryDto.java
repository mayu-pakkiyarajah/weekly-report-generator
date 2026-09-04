package com.teamreports.weeklyreport.dto.report;

import com.teamreports.weeklyreport.entity.enums.Priority;
import com.teamreports.weeklyreport.entity.enums.TaskStatus;
import jakarta.validation.constraints.*;

public record TaskEntryDto(
        Long id,
        @NotBlank @Size(max = 200) String taskName,
        @NotNull Priority priority,
        @Min(0) @Max(100) int plannedPercent,
        @Min(0) @Max(100) int actualPercent,
        @NotNull TaskStatus status,
        @DecimalMin("0.0") double timePlannedHours,
        @DecimalMin("0.0") double timeSpentHours,
        @Size(max = 500) String outputDeliverable
) {
}
