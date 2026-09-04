package com.teamreports.weeklyreport.service;

import com.teamreports.weeklyreport.dto.report.*;
import com.teamreports.weeklyreport.entity.*;
import com.teamreports.weeklyreport.entity.enums.ReportStatus;
import com.teamreports.weeklyreport.entity.enums.ReviewAction;
import com.teamreports.weeklyreport.exception.AccessDeniedBusinessException;
import com.teamreports.weeklyreport.exception.DuplicateResourceException;
import com.teamreports.weeklyreport.exception.InvalidReportStateException;
import com.teamreports.weeklyreport.exception.ResourceNotFoundException;
import com.teamreports.weeklyreport.mapper.ReportMapper;
import com.teamreports.weeklyreport.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Owns the entire report lifecycle: draft creation, content editing, submission,
 * and the manager review/correction cycle - including creating a new immutable
 * {@link ReportVersion} whenever content is edited after a correction request,
 * so past content is never lost.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Set<ReportStatus> EDITABLE_BY_OWNER =
            Set.of(ReportStatus.DRAFT, ReportStatus.NEEDS_CORRECTION);

    private final ReportRepository reportRepository;
    private final ReportVersionRepository reportVersionRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReportMapper reportMapper;

    // ---------- Team member actions ----------

    @Transactional
    public ReportResponse createDraft(Long userId, ReportCreateRequest request) {
        if (reportRepository.findByUserIdAndWeekStartDate(userId, request.weekStartDate()).isPresent()) {
            throw new DuplicateResourceException("A report for this week already exists.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.projectId()));

        Report report = Report.builder()
                .user(user)
                .project(project)
                .weekStartDate(request.weekStartDate())
                .weekEndDate(request.weekEndDate())
                .status(ReportStatus.DRAFT)
                .build();

        ReportVersion version = ReportVersion.builder()
                .report(report)
                .versionNumber(1)
                .build();

        report.getVersions().add(version);
        report.setCurrentVersion(version);

        Report saved = reportRepository.save(report);
        return reportMapper.toResponse(saved);
    }

    @Transactional
    public ReportResponse updateContent(Long userId, Long reportId, ReportContentRequest content) {
        Report report = findReportOrThrow(reportId);
        assertOwner(report, userId);

        if (!EDITABLE_BY_OWNER.contains(report.getStatus())) {
            throw new InvalidReportStateException(
                    "This report can no longer be edited in status " + report.getStatus() + ".");
        }

        ReportVersion targetVersion;
        if (report.getStatus() == ReportStatus.NEEDS_CORRECTION) {
            // First edit after a correction request: snapshot a brand-new version so the
            // version that the manager sent back stays intact and visible in history.
            targetVersion = startNewVersion(report);
        } else {
            targetVersion = report.getCurrentVersion();
        }

        applyContent(targetVersion, content);
        reportVersionRepository.save(targetVersion);
        reportRepository.save(report);

        return reportMapper.toResponse(report);
    }

    @Transactional
    public ReportResponse submit(Long userId, Long reportId) {
        Report report = findReportOrThrow(reportId);
        assertOwner(report, userId);

        if (!EDITABLE_BY_OWNER.contains(report.getStatus())) {
            throw new InvalidReportStateException(
                    "Only draft or needs-correction reports can be submitted (current status: "
                            + report.getStatus() + ").");
        }

        report.getCurrentVersion().setSubmittedAt(Instant.now());
        report.setStatus(ReportStatus.SUBMITTED);
        return reportMapper.toResponse(reportRepository.save(report));
    }

    public ReportResponse getOwnReport(Long userId, Long reportId) {
        Report report = findReportOrThrow(reportId);
        assertOwner(report, userId);
        return reportMapper.toResponse(report);
    }

    public Page<ReportSummaryResponse> listOwnReports(Long userId, Pageable pageable) {
        return reportRepository.findByUserId(userId, pageable).map(reportMapper::toSummary);
    }

    // ---------- Manager actions ----------

    public ReportResponse getReportForManager(Long reportId) {
        return reportMapper.toResponse(findReportOrThrow(reportId));
    }

    public Page<ReportSummaryResponse> searchTeamReports(Long memberId, Long projectId, ReportStatus status,
                                                          LocalDate weekStart, LocalDate weekEnd, Pageable pageable) {
        return reportRepository.search(memberId, projectId, status, weekStart, weekEnd, pageable)
                .map(reportMapper::toSummary);
    }

    @Transactional
    public ReportResponse review(Long managerId, Long reportId, ReviewActionRequest request) {
        Report report = findReportOrThrow(reportId);

        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new InvalidReportStateException(
                    "Only submitted reports can be reviewed (current status: " + report.getStatus() + ").");
        }

        User reviewer = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + managerId));

        ReviewComment comment = ReviewComment.builder()
                .report(report)
                .reportVersion(report.getCurrentVersion())
                .reviewer(reviewer)
                .action(request.action())
                .comment(request.comment())
                .build();
        reviewCommentRepository.save(comment);

        report.setStatus(request.action() == ReviewAction.APPROVED
                ? ReportStatus.APPROVED
                : ReportStatus.NEEDS_CORRECTION);

        return reportMapper.toResponse(reportRepository.save(report));
    }

    public List<ReportVersionResponse> getVersionHistory(Long reportId) {
        // Managers only; enforced at the controller layer via @PreAuthorize.
        findReportOrThrow(reportId);
        return reportVersionRepository.findByReportIdOrderByVersionNumberDesc(reportId).stream()
                .map(reportMapper::toVersionResponse)
                .toList();
    }

    // ---------- internal helpers ----------

    private Report findReportOrThrow(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
    }

    private void assertOwner(Report report, Long userId) {
        if (!report.getUser().getId().equals(userId)) {
            throw new AccessDeniedBusinessException("You can only access your own reports.");
        }
    }

    private ReportVersion startNewVersion(Report report) {
        int nextVersionNumber = reportVersionRepository.countByReportId(report.getId()) + 1;
        ReportVersion version = ReportVersion.builder()
                .report(report)
                .versionNumber(nextVersionNumber)
                .build();
        report.getVersions().add(version);
        report.setCurrentVersion(version);
        return version;
    }

    private void applyContent(ReportVersion version, ReportContentRequest content) {
        version.setTasksPlannedNextWeek(content.tasksPlannedNextWeek());
        version.setNotes(content.notes());
        version.setLinks(content.links());

        version.getTaskEntries().clear();
        if (content.tasksCompleted() != null) {
            content.tasksCompleted().forEach(dto -> version.getTaskEntries().add(
                    TaskEntry.builder()
                            .reportVersion(version)
                            .taskName(dto.taskName())
                            .priority(dto.priority())
                            .plannedPercent(dto.plannedPercent())
                            .actualPercent(dto.actualPercent())
                            .status(dto.status())
                            .timePlannedHours(dto.timePlannedHours())
                            .timeSpentHours(dto.timeSpentHours())
                            .outputDeliverable(dto.outputDeliverable())
                            .build()));
        }

        version.getBlockers().clear();
        if (content.blockers() != null) {
            content.blockers().forEach(dto -> version.getBlockers().add(
                    Blocker.builder()
                            .reportVersion(version)
                            .description(dto.description())
                            .keyIssue(dto.keyIssue())
                            .build()));
        }

        version.getAchievements().clear();
        if (content.achievements() != null) {
            content.achievements().forEach(dto -> version.getAchievements().add(
                    Achievement.builder()
                            .reportVersion(version)
                            .description(dto.description())
                            .keyAchievement(dto.keyAchievement())
                            .build()));
        }

        version.getHoursEntries().clear();
        if (content.hoursByTaskType() != null) {
            content.hoursByTaskType().forEach(dto -> version.getHoursEntries().add(
                    HoursEntry.builder()
                            .reportVersion(version)
                            .taskType(dto.taskType())
                            .hours(dto.hours())
                            .build()));
        }
    }
}
