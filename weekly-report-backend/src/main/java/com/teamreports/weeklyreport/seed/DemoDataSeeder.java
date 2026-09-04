package com.teamreports.weeklyreport.seed;

import com.teamreports.weeklyreport.config.SeedProperties;
import com.teamreports.weeklyreport.entity.*;
import com.teamreports.weeklyreport.entity.enums.*;
import com.teamreports.weeklyreport.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Populates a realistic demo dataset (5 team members, 1 manager, 3 projects, several weeks
 * of reports across every status) so the dashboard and review workflow are meaningful to
 * evaluate out of the box. Only runs when app.seed.enabled=true AND the users table is empty,
 * so it never silently re-seeds or duplicates data in a real environment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private final SeedProperties seedProperties;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReportRepository reportRepository;
    private final ReportVersionRepository reportVersionRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedProperties.enabled()) {
            return;
        }
        if (userRepository.count() > 0) {
            log.info("Seed skipped: users table is not empty.");
            return;
        }

        log.info("Seeding demo dataset...");

        User manager = userRepository.save(user("Amara Fernando", "manager@demo.local", Role.MANAGER));

        List<User> members = List.of(
                userRepository.save(user("Kavindu Perera", "kavindu@demo.local", Role.TEAM_MEMBER)),
                userRepository.save(user("Nadeesha Silva", "nadeesha@demo.local", Role.TEAM_MEMBER)),
                userRepository.save(user("Ruwan Jayasuriya", "ruwan@demo.local", Role.TEAM_MEMBER)),
                userRepository.save(user("Ishara Wickramasinghe", "ishara@demo.local", Role.TEAM_MEMBER)),
                userRepository.save(user("Tharindu Bandara", "tharindu@demo.local", Role.TEAM_MEMBER))
        );

        List<Project> projects = List.of(
                projectRepository.save(project("Client A", "External client engagement - retail platform")),
                projectRepository.save(project("Internal Tooling", "Internal developer productivity tools")),
                projectRepository.save(project("R&D", "Exploratory / research and prototyping work"))
        );

        LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Go back 4 full weeks plus the current (in-progress) week.
        for (int weeksAgo = 4; weeksAgo >= 0; weeksAgo--) {
            LocalDate weekStart = currentWeekStart.minusWeeks(weeksAgo);
            LocalDate weekEnd = weekStart.plusDays(6);

            for (int i = 0; i < members.size(); i++) {
                User member = members.get(i);
                Project project = projects.get(i % projects.size());

                // Vary status by member/week so every status is represented on the dashboard.
                ReportStatus targetStatus = pickStatus(weeksAgo, i);
                if (targetStatus == null) {
                    continue; // simulates "not yet started" for this member/week
                }

                seedReport(member, manager, project, weekStart, weekEnd, targetStatus, i);
            }
        }

        log.info("Demo dataset seeded: {} users, {} projects, {} reports.",
                userRepository.count(), projectRepository.count(), reportRepository.count());
    }

    private ReportStatus pickStatus(int weeksAgo, int memberIndex) {
        if (weeksAgo == 0) {
            // Current week: mix of not-started, draft and submitted - a realistic in-flight week.
            return switch (memberIndex % 5) {
                case 0 -> ReportStatus.SUBMITTED;
                case 1 -> ReportStatus.DRAFT;
                case 2 -> null; // not started
                case 3 -> ReportStatus.SUBMITTED;
                default -> ReportStatus.DRAFT;
            };
        }
        if (weeksAgo == 1) {
            return switch (memberIndex % 5) {
                case 0 -> ReportStatus.NEEDS_CORRECTION;
                case 1 -> ReportStatus.APPROVED;
                case 2 -> ReportStatus.APPROVED;
                case 3 -> ReportStatus.SUBMITTED;
                default -> ReportStatus.APPROVED;
            };
        }
        // Older weeks: mostly approved, as a completed history.
        return ReportStatus.APPROVED;
    }

    private void seedReport(User member, User manager, Project project, LocalDate weekStart, LocalDate weekEnd,
                            ReportStatus targetStatus, int seedVariant) {

        Report report = Report.builder()
                .user(member)
                .project(project)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .status(ReportStatus.DRAFT)
                .build();

        // 1. Create and SAVE the Report first
        Report savedReport = reportRepository.save(report);

        // 2. Create ReportVersion v1
        ReportVersion v1 = buildVersion(savedReport, 1, seedVariant, false);
        savedReport.getVersions().add(v1);
        savedReport.setCurrentVersion(v1);

        // 3. SAVE the ReportVersion BEFORE using it in ReviewComments
        ReportVersion savedV1 = reportVersionRepository.save(v1);
        reportRepository.save(savedReport);

        if (targetStatus == ReportStatus.DRAFT) {
            return;
        }

        // 4. Submit version 1
        savedV1.setSubmittedAt(weekEnd.atTime(16, 0).toInstant(java.time.ZoneOffset.UTC));
        savedReport.setStatus(ReportStatus.SUBMITTED);
        reportRepository.save(savedReport);

        if (targetStatus == ReportStatus.SUBMITTED) {
            return;
        }

        if (targetStatus == ReportStatus.NEEDS_CORRECTION) {
            // 5. Now we can safely create ReviewComment with savedV1
            reviewCommentRepository.save(ReviewComment.builder()
                    .report(savedReport)
                    .reportVersion(savedV1)  // ✅ Now it's persisted!
                    .reviewer(manager)
                    .action(ReviewAction.CHANGES_REQUESTED)
                    .comment("Please add more detail on the blocker for the payment integration task, "
                            + "and double check the planned vs actual percentages.")
                    .build());
            savedReport.setStatus(ReportStatus.NEEDS_CORRECTION);
            reportRepository.save(savedReport);
            return;
        }

        // APPROVED: for variety, make some approved reports go through one correction cycle first
        if (seedVariant % 2 == 0) {
            // 6. Create ReviewComment with savedV1
            reviewCommentRepository.save(ReviewComment.builder()
                    .report(savedReport)
                    .reportVersion(savedV1)  // ✅ Saved!
                    .reviewer(manager)
                    .action(ReviewAction.CHANGES_REQUESTED)
                    .comment("Good progress, but please clarify the achievement description before we sign off.")
                    .build());

            // 7. Create Version 2
            ReportVersion v2 = buildVersion(savedReport, 2, seedVariant, true);
            savedReport.getVersions().add(v2);
            savedReport.setCurrentVersion(v2);

            // 8. SAVE Version 2
            ReportVersion savedV2 = reportVersionRepository.save(v2);
            savedV2.setSubmittedAt(weekEnd.atTime(17, 30).toInstant(java.time.ZoneOffset.UTC));
            savedReport.setStatus(ReportStatus.SUBMITTED);
            reportRepository.save(savedReport);

            // 9. ReviewComment for Version 2 - now with savedV2
            reviewCommentRepository.save(ReviewComment.builder()
                    .report(savedReport)
                    .reportVersion(savedV2)  // ✅ Saved!
                    .reviewer(manager)
                    .action(ReviewAction.APPROVED)
                    .comment("Looks good now, approved.")
                    .build());
        } else {
            // 10. ReviewComment with savedV1
            reviewCommentRepository.save(ReviewComment.builder()
                    .report(savedReport)
                    .reportVersion(savedV1)  // ✅ Saved!
                    .reviewer(manager)
                    .action(ReviewAction.APPROVED)
                    .comment("Solid week, approved.")
                    .build());
        }
        savedReport.setStatus(ReportStatus.APPROVED);
        reportRepository.save(savedReport);
    }

    private ReportVersion buildVersion(Report report, int versionNumber, int seedVariant, boolean isCorrectedVersion) {
        ReportVersion version = ReportVersion.builder()
                .report(report)
                .versionNumber(versionNumber)
                .tasksPlannedNextWeek(isCorrectedVersion
                        ? "Continue integration testing; start documentation for the new module."
                        : "Finish API integration; begin writing unit tests.")
                .notes("Week went smoothly overall.")
                .links("https://internal.example.com/wiki/weekly-notes")
                .build();

        version.getTaskEntries().add(TaskEntry.builder()
                .reportVersion(version)
                .taskName("Implement payment integration")
                .priority(Priority.HIGH)
                .plannedPercent(100)
                .actualPercent(isCorrectedVersion ? 100 : 80)
                .status(isCorrectedVersion ? TaskStatus.COMPLETED : TaskStatus.IN_PROGRESS)
                .timePlannedHours(20)
                .timeSpentHours(18)
                .outputDeliverable("PR #" + (100 + seedVariant))
                .build());

        version.getTaskEntries().add(TaskEntry.builder()
                .reportVersion(version)
                .taskName("Write test coverage for reporting module")
                .priority(Priority.MEDIUM)
                .plannedPercent(80)
                .actualPercent(70)
                .status(TaskStatus.IN_PROGRESS)
                .timePlannedHours(10)
                .timeSpentHours(8)
                .outputDeliverable("Test suite update")
                .build());

        version.getBlockers().add(Blocker.builder()
                .reportVersion(version)
                .description(isCorrectedVersion
                        ? "Payment gateway sandbox was intermittently unavailable early in the week; resolved by Wednesday."
                        : "Waiting on updated API credentials from the payment gateway vendor.")
                .keyIssue(true)
                .build());

        version.getAchievements().add(Achievement.builder()
                .reportVersion(version)
                .description(isCorrectedVersion
                        ? "Completed and verified the full payment integration end-to-end."
                        : "Delivered the first working version of the payment integration flow.")
                .keyAchievement(true)
                .build());

        version.getHoursEntries().add(HoursEntry.builder()
                .reportVersion(version).taskType(TaskType.DEVELOPMENT).hours(22).build());
        version.getHoursEntries().add(HoursEntry.builder()
                .reportVersion(version).taskType(TaskType.TESTING).hours(8).build());
        version.getHoursEntries().add(HoursEntry.builder()
                .reportVersion(version).taskType(TaskType.MEETINGS).hours(6).build());
        version.getHoursEntries().add(HoursEntry.builder()
                .reportVersion(version).taskType(TaskType.DOCUMENTATION).hours(4).build());

        return version;
    }

    private User user(String fullName, String email, Role role) {
        return User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(role)
                .active(true)
                .build();
    }

    private Project project(String name, String description) {
        return Project.builder().name(name).description(description).active(true).build();
    }
}
