package com.teamreports.weeklyreport.dto.dashboard;

public record DashboardSummaryResponse(
        long totalReportsSubmitted,
        long expectedReports,
        long pendingCount,
        long lateCount,
        double complianceRatePercent,
        long needsCorrectionCount,
        long openBlockersCount
) {
}
