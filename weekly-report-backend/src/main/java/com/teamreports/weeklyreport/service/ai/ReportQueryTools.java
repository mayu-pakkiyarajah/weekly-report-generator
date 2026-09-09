package com.teamreports.weeklyreport.service.ai;

import com.teamreports.weeklyreport.entity.Report;
import com.teamreports.weeklyreport.entity.ReportVersion;
import com.teamreports.weeklyreport.entity.User;
import com.teamreports.weeklyreport.entity.enums.Role;
import com.teamreports.weeklyreport.repository.ReportRepository;
import com.teamreports.weeklyreport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * The concrete data functions exposed to the AI assistant as "tools".
 *
 * Data-privacy note: every method here only reads data a manager can already see
 * through the normal dashboard/report screens (team members, projects, report content).
 * Nothing outside that surface - no password hashes, no other manager's private notes,
 * no data belonging to a different organization - is ever assembled for the model.
 */
@Component
@RequiredArgsConstructor
public class ReportQueryTools {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public record WeekMemberSnapshot(
            String memberName, String projectName, String status,
            List<String> tasksCompleted, List<String> blockers, List<String> achievements) {
    }

    public record MemberWeekEntry(
            String weekStartDate, String projectName, String status,
            List<String> tasksCompleted, List<String> blockers, List<String> achievements) {
    }

    public record MemberHistoryResult(String matchedMemberName, List<MemberWeekEntry> weeks) {
    }

    /** Everyone's report for a given week - the "what did the team work on" tool. */
    public List<WeekMemberSnapshot> weekSnapshot(LocalDate weekStart) {
        return reportRepository.findByWeekStartDate(weekStart).stream()
                .map(this::toSnapshot)
                .toList();
    }

    /** One member's reports over the last N weeks - the "what did X work on" tool. */
    public MemberHistoryResult memberHistory(String nameFragment, int weeksBack) {
        String needle = nameFragment == null ? "" : nameFragment.toLowerCase(Locale.ROOT).trim();

        User match = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TEAM_MEMBER)
                .filter(u -> u.getFullName().toLowerCase(Locale.ROOT).contains(needle))
                .findFirst()
                .orElse(null);

        if (match == null) {
            return new MemberHistoryResult(null, List.of());
        }

        LocalDate cutoff = LocalDate.now().minusWeeks(Math.max(1, weeksBack));

        List<MemberWeekEntry> weeks = reportRepository.findByUserId(match.getId(),
                        org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(r -> !r.getWeekStartDate().isBefore(cutoff))
                .sorted((a, b) -> b.getWeekStartDate().compareTo(a.getWeekStartDate()))
                .map(this::toMemberWeekEntry)
                .toList();

        return new MemberHistoryResult(match.getFullName(), weeks);
    }

    private WeekMemberSnapshot toSnapshot(Report report) {
        ReportVersion v = report.getCurrentVersion();
        return new WeekMemberSnapshot(
                report.getUser().getFullName(),
                report.getProject().getName(),
                report.getStatus().name(),
                v == null ? List.of() : v.getTaskEntries().stream()
                        .map(t -> t.getTaskName() + " (" + t.getStatus() + ", " + t.getActualPercent() + "% done)")
                        .toList(),
                v == null ? List.of() : v.getBlockers().stream().map(b -> b.getDescription()
                        + (b.isKeyIssue() ? " [key issue]" : "")).toList(),
                v == null ? List.of() : v.getAchievements().stream().map(a -> a.getDescription()
                        + (a.isKeyAchievement() ? " [key achievement]" : "")).toList());
    }

    private MemberWeekEntry toMemberWeekEntry(Report report) {
        ReportVersion v = report.getCurrentVersion();
        return new MemberWeekEntry(
                report.getWeekStartDate().toString(),
                report.getProject().getName(),
                report.getStatus().name(),
                v == null ? List.of() : v.getTaskEntries().stream()
                        .map(t -> t.getTaskName() + " (" + t.getStatus() + ")").toList(),
                v == null ? List.of() : v.getBlockers().stream().map(b -> b.getDescription()).toList(),
                v == null ? List.of() : v.getAchievements().stream().map(a -> a.getDescription()).toList());
    }
}
