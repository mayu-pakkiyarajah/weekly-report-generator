package com.teamreports.weeklyreport.dto.dashboard;

import com.teamreports.weeklyreport.entity.enums.TaskType;

public record TimeByTaskTypeResponse(TaskType taskType, Double totalHours) {
}
