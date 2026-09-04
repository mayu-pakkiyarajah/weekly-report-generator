package com.teamreports.weeklyreport.dto.dashboard;

public record WorkloadByProjectResponse(Long projectId, String projectName, Long taskCount, Double totalHours) {
}
