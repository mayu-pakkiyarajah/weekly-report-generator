package com.teamreports.weeklyreport.dto.dashboard;

import java.time.Instant;

public record ActivityFeedItemResponse(
        String type,
        Long reportId,
        String userFullName,
        String description,
        Instant timestamp
) {
}
