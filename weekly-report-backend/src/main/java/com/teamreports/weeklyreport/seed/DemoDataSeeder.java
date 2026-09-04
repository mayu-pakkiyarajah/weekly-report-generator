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

        for (int weeksAgo = 4; weeksAgo >= 0; weeksAgo--) {
            LocalDate weekStart = currentWeekStart.minusWeeks(weeksAgo);
            LocalDate weekEnd = weekStart.plusDays(6);

            for (int i = 0; i < members.size(); i++) {
                User member = members.get(i);
                Project project = projects.get(i % projects.size());

                ReportStatus targetStatus = pickStatus(weeksAgo, i);
                if (targetStatus == null) {
                    continue;
                }

                seedReport(member, manager, project, weekStart, weekEnd, targetStatus, i);
            }
        }

        log.info("Demo dataset seeded: {} users, {} projects, {} reports.",
                userRepository.count(), projectRepository.count(), reportRepository.count());
    }

    private ReportStatus pickStatus(int weeksAgo, int memberIndex) {
        if (weeksAgo == 0) {
            return switch (memberIndex % 5) {
                case 0 -> ReportStatus.SUBMITTED;
                case 1 -> ReportStatus.DRAFT;
                case 2 -> null;
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

        Report savedReport = reportRepository.save(report);

        ReportVersion v1 = buildVersion(savedReport, 1, seedVariant, false);
        savedReport.getVersions().add(v1);
        savedReport.setCurrentVersion(v1);

        ReportVersion savedV1 = reportVersionRepository.save(v1);
        reportRepository.save(savedReport);

        if (targetStatus == ReportStatus.DRAFT) {
            return;
        }

        savedV1.setSubmittedAt(weekEnd.atTime(16, 0).toInstant(java.time.ZoneOffset.UTC));
        savedReport.setStatus(ReportStatus.SUBMITTED);
        reportRepository.save(savedReport);

        if (targetStatus == ReportStatus.SUBMITTED) {
            return;
        }

        if (targetStatus == ReportStatus.NEEDS_CORRECTION) {

            reviewCommentRepository.save(ReviewComment.builder()
                    .report(savedReport)
                    .reportVersion(savedV1)
                    .reviewer(manager)
                    .action(ReviewAction.CHANGES_REQUESTED)
                    .comment("Please add more detail on the blocker for the payment integration task, "
                            + "and double check the planned vs actual percentages.")
                    .build());
            savedReport.setStatus(ReportStatus.NEEDS_CORRECTION);
            reportRepository.save(savedReport);
            return;
        }

        if (seedVariant % 2 == 0) {

            reviewCommentRepository.save(ReviewComment.builder()
                    .report(savedReport)
                    .reportVersion(savedV1)
                    .reviewer(manager)
                    .action(ReviewAction.CHANGES_REQUESTED)
                    .comment("Good progress, but please clarify the achievement description before we sign off.")
                    .build());

            ReportVersion v2 = buildVersion(savedReport, 2, seedVariant, true);
            savedReport.getVersions().add(v2);
            savedReport.setCurrentVersion(v2);

            ReportVersion savedV2 = reportVersionRepository.save(v2);
            savedV2.setSubmittedAt(weekEnd.atTime(17, 30).toInstant(java.time.ZoneOffset.UTC));
            savedReport.setStatus(ReportStatus.SUBMITTED);
            reportRepository.save(savedReport);

            reviewCommentRepository.save(ReviewComment.builder()
                    .report(savedReport)
                    .reportVersion(savedV2)
                    .reviewer(manager)
                    .action(ReviewAction.APPROVED)
                    .comment("Looks good now, approved.")
                    .build());
        } else {
            reviewCommentRepository.save(ReviewComment.builder()
                    .report(savedReport)
                    .reportVersion(savedV1)
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
