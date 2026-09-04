package com.teamreports.weeklyreport.dto.report;

import java.time.Instant;
import java.util.List;

public record ReportVersionResponse(
        Long id,
        int versionNumber,
        List<TaskEntryDto> tasksCompleted,
        String tasksPlannedNextWeek,
        List<BlockerDto> blockers,
        List<AchievementDto> achievements,
        List<HoursEntryDto> hoursByTaskType,
        String notes,
        String links,
        Instant submittedAt
) {
}
