package com.teamreports.weeklyreport.dto.report;

import com.teamreports.weeklyreport.entity.enums.ReportStatus;

import java.time.LocalDate;

/** Lightweight row used in list/history/dashboard views. */
public record ReportSummaryResponse(
        Long id,
        Long userId,
        String userFullName,
        Long projectId,
        String projectName,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        ReportStatus status,
        int versionCount,
        boolean hasOpenBlockers
) {
}
