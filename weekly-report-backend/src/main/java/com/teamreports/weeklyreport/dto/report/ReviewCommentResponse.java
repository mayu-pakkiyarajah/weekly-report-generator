package com.teamreports.weeklyreport.dto.report;

import com.teamreports.weeklyreport.entity.enums.ReviewAction;

import java.time.Instant;

public record ReviewCommentResponse(
        Long id,
        int reviewedVersionNumber,
        ReviewAction action,
        String comment,
        String reviewerName,
        Instant createdAt
) {
}
