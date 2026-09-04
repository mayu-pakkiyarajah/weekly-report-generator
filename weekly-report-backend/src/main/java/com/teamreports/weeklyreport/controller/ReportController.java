package com.teamreports.weeklyreport.controller;

import com.teamreports.weeklyreport.dto.report.*;
import com.teamreports.weeklyreport.security.CurrentUserProvider;
import com.teamreports.weeklyreport.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Team-member-facing report endpoints. Every method resolves the acting user from the
 * authenticated JWT (never from a client-supplied id), so a team member can never read
 * or write another team member's report through this controller.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<ReportResponse> createDraft(@Valid @RequestBody ReportCreateRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createDraft(userId, request));
    }

    @GetMapping
    public Page<ReportSummaryResponse> listOwnReports(Pageable pageable) {
        return reportService.listOwnReports(currentUserProvider.getCurrentUserId(), pageable);
    }

    @GetMapping("/{id}")
    public ReportResponse getOwnReport(@PathVariable Long id) {
        return reportService.getOwnReport(currentUserProvider.getCurrentUserId(), id);
    }

    @PutMapping("/{id}")
    public ReportResponse updateContent(@PathVariable Long id, @Valid @RequestBody ReportContentRequest request) {
        return reportService.updateContent(currentUserProvider.getCurrentUserId(), id, request);
    }

    @PostMapping("/{id}/submit")
    public ReportResponse submit(@PathVariable Long id) {
        return reportService.submit(currentUserProvider.getCurrentUserId(), id);
    }
}
