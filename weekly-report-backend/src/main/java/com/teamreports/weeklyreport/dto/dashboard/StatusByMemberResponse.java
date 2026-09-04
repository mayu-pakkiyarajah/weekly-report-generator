package com.teamreports.weeklyreport.dto.dashboard;

import com.teamreports.weeklyreport.entity.enums.ReportStatus;

public record StatusByMemberResponse(Long userId, String userFullName, ReportStatus status) {
}
