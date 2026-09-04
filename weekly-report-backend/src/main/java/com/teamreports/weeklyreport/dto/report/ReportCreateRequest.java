package com.teamreports.weeklyreport.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Creates a new blank draft report for a given week + project. */
public record ReportCreateRequest(
        @NotNull LocalDate weekStartDate,
        @NotNull LocalDate weekEndDate,
        @NotNull Long projectId
) {
}
