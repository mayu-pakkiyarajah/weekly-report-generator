package com.teamreports.weeklyreport.dto.report;

import com.teamreports.weeklyreport.entity.enums.ReportStatus;

import java.time.LocalDate;
import java.util.List;

/** Full detail view of a report: identity + the currently-active version + full review history. */
public record ReportResponse(
        Long id,
        Long userId,
        String userFullName,
        Long projectId,
        String projectName,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        ReportStatus status,
        ReportVersionResponse currentVersion,
        List<ReviewCommentResponse> reviewHistory
) {
}
