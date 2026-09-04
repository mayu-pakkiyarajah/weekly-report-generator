package com.teamreports.weeklyreport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Controls when a not-yet-submitted report is considered "late" for compliance metrics. */
@ConfigurationProperties(prefix = "app.submission")
public record SubmissionProperties(int deadlineDayOfWeek, int deadlineHour) {
}
