package com.teamreports.weeklyreport.entity.enums;

/**
 * Lifecycle of a weekly report.
 * DRAFT -> SUBMITTED -> (APPROVED | NEEDS_CORRECTION)
 * NEEDS_CORRECTION -> SUBMITTED (after the team member edits and resubmits)
 */
public enum ReportStatus {
    DRAFT,
    SUBMITTED,
    NEEDS_CORRECTION,
    APPROVED
}
