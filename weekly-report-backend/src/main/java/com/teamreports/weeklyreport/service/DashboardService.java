package com.teamreports.weeklyreport.service;

import com.teamreports.weeklyreport.dto.dashboard.*;
import com.teamreports.weeklyreport.entity.Achievement;
import com.teamreports.weeklyreport.entity.Blocker;
import com.teamreports.weeklyreport.entity.Report;
import com.teamreports.weeklyreport.entity.ReviewComment;
import com.teamreports.weeklyreport.entity.enums.ReportStatus;
import com.teamreports.weeklyreport.entity.enums.Role;
import com.teamreports.weeklyreport.entity.enums.TaskStatus;
import com.teamreports.weeklyreport.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final TaskEntryRepository taskEntryRepository;
    private final HoursEntryRepository hoursEntryRepository;
    private final BlockerRepository blockerRepository;
    private final AchievementRepository achievementRepository;

    public DashboardSummaryResponse getSummary(LocalDate weekStart) {
        long expected = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TEAM_MEMBER && u.isActive())
                .count();

        List<Report> weekReports = reportRepository.findByWeekStartDate(weekStart);

        long submitted = weekReports.stream()
                .filter(r -> r.getStatus() != ReportStatus.DRAFT)
                .count();

        long needsCorrection = weekReports.stream()
                .filter(r -> r.getStatus() == ReportStatus.NEEDS_CORRECTION)
                .count();

        boolean deadlinePassed = LocalDateTime.now(ZoneOffset.UTC)
                .isAfter(weekStart.plusDays(6).atTime(23, 59));

        long late = deadlinePassed ? Math.max(0, expected - submitted) : 0;
        long pending = deadlinePassed ? 0 : Math.max(0, expected - submitted);

        double complianceRate = expected == 0 ? 0.0 : (submitted * 100.0) / expected;

        long openBlockers = blockerRepository.countOpenBlockersForWeek(weekStart);

        return new DashboardSummaryResponse(submitted, expected, pending, late,
                Math.round(complianceRate * 10.0) / 10.0, needsCorrection, openBlockers);
    }

    public List<StatusByMemberResponse> getStatusByMember(LocalDate weekStart) {
        Map<Long, Report> reportsByUser = reportRepository.findByWeekStartDate(weekStart).stream()
                .collect(Collectors.toMap(r -> r.getUser().getId(), r -> r));

        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TEAM_MEMBER && u.isActive())
                .map(u -> {
                    Report report = reportsByUser.get(u.getId());
                    ReportStatus status = report != null ? report.getStatus() : null;
                    return new StatusByMemberResponse(u.getId(), u.getFullName(), status);
                })
                .sorted(Comparator.comparing(StatusByMemberResponse::userFullName))
                .toList();
    }

    public List<WorkloadByProjectResponse> getWorkloadByProject(LocalDate weekStart) {
        return taskEntryRepository.findWorkloadByProject(weekStart);
    }

    public List<TimeByTaskTypeResponse> getTimeByTaskType(LocalDate weekStart) {
        return hoursEntryRepository.findHoursByTaskType(weekStart);
    }

    public List<TasksTrendPointResponse> getTasksCompletedTrend(int weeksBack, Long userId) {
        LocalDate fromWeek = LocalDate.now().minusWeeks(weeksBack)
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return taskEntryRepository.findCompletionTrend(TaskStatus.COMPLETED, fromWeek, userId);
    }

    public List<ActivityFeedItemResponse> getActivityFeed(int limit) {
        return reviewCommentRepository.findAll().stream()
                .sorted(Comparator.comparing((ReviewComment c) -> c.getCreatedAt()).reversed())
                .limit(limit)
                .map(c -> new ActivityFeedItemResponse(
                        c.getAction().name(),
                        c.getReport().getId(),
                        c.getReport().getUser().getFullName(),
                        c.getAction() == com.teamreports.weeklyreport.entity.enums.ReviewAction.APPROVED
                                ? "Report approved by " + c.getReviewer().getFullName()
                                : "Sent back for correction by " + c.getReviewer().getFullName(),
                        c.getCreatedAt()))
                .toList();
    }

    /** Bonus: one section across the whole team for a given week, side by side. */
    public List<SectionAcrossTeamResponse> getBlockersAcrossTeam(LocalDate weekStart) {
        Map<Long, List<Blocker>> byUser = blockerRepository.findAllForWeek(weekStart).stream()
                .collect(Collectors.groupingBy(b -> b.getReportVersion().getReport().getUser().getId()));

        return toSectionResponse(byUser, blockerRepository.findAllForWeek(weekStart).stream()
                .map(b -> b.getReportVersion().getReport().getUser())
                .distinct().toList(), Blocker::getDescription);
    }

    public List<SectionAcrossTeamResponse> getAchievementsAcrossTeam(LocalDate weekStart) {
        Map<Long, List<Achievement>> byUser = achievementRepository.findAllForWeek(weekStart).stream()
                .collect(Collectors.groupingBy(a -> a.getReportVersion().getReport().getUser().getId()));

        return toSectionResponse(byUser, achievementRepository.findAllForWeek(weekStart).stream()
                .map(a -> a.getReportVersion().getReport().getUser())
                .distinct().toList(), Achievement::getDescription);
    }

    private <T> List<SectionAcrossTeamResponse> toSectionResponse(
            Map<Long, List<T>> byUser, List<com.teamreports.weeklyreport.entity.User> users,
            java.util.function.Function<T, String> descriptionExtractor) {

        Map<Long, String> namesById = new HashMap<>();
        users.forEach(u -> namesById.put(u.getId(), u.getFullName()));

        return byUser.entrySet().stream()
                .map(e -> new SectionAcrossTeamResponse(
                        e.getKey(),
                        namesById.get(e.getKey()),
                        e.getValue().stream().map(descriptionExtractor).toList()))
                .sorted(Comparator.comparing(SectionAcrossTeamResponse::userFullName))
                .toList();
    }
}