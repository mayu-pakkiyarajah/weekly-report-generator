package com.teamreports.weeklyreport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.submission")
public record SubmissionProperties(int deadlineDayOfWeek, int deadlineHour) {
}
