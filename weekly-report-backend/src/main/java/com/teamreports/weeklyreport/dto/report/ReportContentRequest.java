package com.teamreports.weeklyreport.dto.report;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReportContentRequest(
        @Valid List<TaskEntryDto> tasksCompleted,
        @Size(max = 4000) String tasksPlannedNextWeek,
        @Valid List<BlockerDto> blockers,
        @Valid List<AchievementDto> achievements,
        @Valid List<HoursEntryDto> hoursByTaskType,
        @Size(max = 4000) String notes,
        @Size(max = 1000) String links
) {
}
