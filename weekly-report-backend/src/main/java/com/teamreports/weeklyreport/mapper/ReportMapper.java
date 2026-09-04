package com.teamreports.weeklyreport.mapper;

import com.teamreports.weeklyreport.dto.report.*;
import com.teamreports.weeklyreport.entity.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Hand-written mapper (rather than MapStruct) for the report aggregate:
 * the nested version/task/blocker/achievement/hours structure benefits from
 * explicit, readable control over ordering and null-safety.
 */
@Component
public class ReportMapper {

    public ReportVersionResponse toVersionResponse(ReportVersion v) {
        if (v == null) return null;

        List<TaskEntryDto> tasks = v.getTaskEntries().stream()
                .map(t -> new TaskEntryDto(t.getId(), t.getTaskName(), t.getPriority(),
                        t.getPlannedPercent(), t.getActualPercent(), t.getStatus(),
                        t.getTimePlannedHours(), t.getTimeSpentHours(), t.getOutputDeliverable()))
                .toList();

        List<BlockerDto> blockers = v.getBlockers().stream()
                .map(b -> new BlockerDto(b.getId(), b.getDescription(), b.isKeyIssue()))
                .toList();

        List<AchievementDto> achievements = v.getAchievements().stream()
                .map(a -> new AchievementDto(a.getId(), a.getDescription(), a.isKeyAchievement()))
                .toList();

        List<HoursEntryDto> hours = v.getHoursEntries().stream()
                .map(h -> new HoursEntryDto(h.getTaskType(), h.getHours()))
                .toList();

        return new ReportVersionResponse(v.getId(), v.getVersionNumber(), tasks,
                v.getTasksPlannedNextWeek(), blockers, achievements, hours,
                v.getNotes(), v.getLinks(), v.getSubmittedAt());
    }

    public ReviewCommentResponse toReviewCommentResponse(ReviewComment c) {
        return new ReviewCommentResponse(
                c.getId(),
                c.getReportVersion().getVersionNumber(),
                c.getAction(),
                c.getComment(),
                c.getReviewer().getFullName(),
                c.getCreatedAt());
    }

    public ReportResponse toResponse(Report report) {
        List<ReviewCommentResponse> history = report.getReviewComments().stream()
                .sorted(Comparator.comparing(ReviewComment::getCreatedAt).reversed())
                .map(this::toReviewCommentResponse)
                .toList();

        return new ReportResponse(
                report.getId(),
                report.getUser().getId(),
                report.getUser().getFullName(),
                report.getProject().getId(),
                report.getProject().getName(),
                report.getWeekStartDate(),
                report.getWeekEndDate(),
                report.getStatus(),
                toVersionResponse(report.getCurrentVersion()),
                history);
    }

    public ReportSummaryResponse toSummary(Report report) {
        boolean hasOpenBlockers = report.getCurrentVersion() != null
                && !report.getCurrentVersion().getBlockers().isEmpty();

        return new ReportSummaryResponse(
                report.getId(),
                report.getUser().getId(),
                report.getUser().getFullName(),
                report.getProject().getId(),
                report.getProject().getName(),
                report.getWeekStartDate(),
                report.getWeekEndDate(),
                report.getStatus(),
                report.getVersions().size(),
                hasOpenBlockers);
    }
}
