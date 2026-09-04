package com.teamreports.weeklyreport.dto.dashboard;

import java.time.LocalDate;

public record TasksTrendPointResponse(LocalDate weekStartDate, Long tasksCompletedCount) {
}
