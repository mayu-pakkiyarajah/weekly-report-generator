package com.teamreports.weeklyreport.controller;

import com.teamreports.weeklyreport.dto.dashboard.*;
import com.teamreports.weeklyreport.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Manager-only dashboard metrics and chart data. All week parameters expect the Monday of that week. */
@RestController
@RequestMapping("/api/manager/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return dashboardService.getSummary(weekStart);
    }

    @GetMapping("/status-by-member")
    public List<StatusByMemberResponse> getStatusByMember(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return dashboardService.getStatusByMember(weekStart);
    }

    @GetMapping("/workload-by-project")
    public List<WorkloadByProjectResponse> getWorkloadByProject(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return dashboardService.getWorkloadByProject(weekStart);
    }

    @GetMapping("/time-by-task-type")
    public List<TimeByTaskTypeResponse> getTimeByTaskType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return dashboardService.getTimeByTaskType(weekStart);
    }

    @GetMapping("/tasks-completed-trend")
    public List<TasksTrendPointResponse> getTasksCompletedTrend(
            @RequestParam(defaultValue = "8") int weeksBack,
            @RequestParam(required = false) Long userId) {
        return dashboardService.getTasksCompletedTrend(weeksBack, userId);
    }

    @GetMapping("/activity-feed")
    public List<ActivityFeedItemResponse> getActivityFeed(@RequestParam(defaultValue = "20") int limit) {
        return dashboardService.getActivityFeed(limit);
    }

    @GetMapping("/sections/blockers")
    public List<SectionAcrossTeamResponse> getBlockersAcrossTeam(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return dashboardService.getBlockersAcrossTeam(weekStart);
    }

    @GetMapping("/sections/achievements")
    public List<SectionAcrossTeamResponse> getAchievementsAcrossTeam(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return dashboardService.getAchievementsAcrossTeam(weekStart);
    }
}
