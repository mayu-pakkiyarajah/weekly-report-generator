package com.teamreports.weeklyreport.controller;

import com.teamreports.weeklyreport.dto.report.*;
import com.teamreports.weeklyreport.entity.enums.ReportStatus;
import com.teamreports.weeklyreport.security.CurrentUserProvider;
import com.teamreports.weeklyreport.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Manager-only: view any team member's reports and drive the review/correction workflow. */
@RestController
@RequestMapping("/api/manager/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerReportController {

    private final ReportService reportService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public Page<ReportSummaryResponse> searchReports(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd,
            Pageable pageable) {
        return reportService.searchTeamReports(memberId, projectId, status, weekStart, weekEnd, pageable);
    }

    @GetMapping("/{id}")
    public ReportResponse getReport(@PathVariable Long id) {
        return reportService.getReportForManager(id);
    }

    @GetMapping("/{id}/versions")
    public List<ReportVersionResponse> getVersionHistory(@PathVariable Long id) {
        return reportService.getVersionHistory(id);
    }

    @PostMapping("/{id}/review")
    public ReportResponse review(@PathVariable Long id, @Valid @RequestBody ReviewActionRequest request) {
        Long managerId = currentUserProvider.getCurrentUserId();
        return reportService.review(managerId, id, request);
    }
}
