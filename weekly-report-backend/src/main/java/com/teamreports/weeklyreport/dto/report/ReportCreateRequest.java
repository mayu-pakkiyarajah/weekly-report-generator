package com.teamreports.weeklyreport.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReportCreateRequest(
        @NotNull LocalDate weekStartDate,
        @NotNull LocalDate weekEndDate,
        @NotNull Long projectId
) {
}
