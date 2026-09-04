package com.teamreports.weeklyreport.entity.enums;

/**
 * Categories used for the "hours worked by task type" breakdown.
 * Kept as an enum (rather than a free-text field) so the team-wide
 * "time spent by task type" chart on the dashboard is always comparable.
 */
public enum TaskType {
    DEVELOPMENT,
    TESTING,
    MEETINGS,
    DOCUMENTATION,
    CODE_REVIEW,
    RESEARCH,
    OTHER
}
