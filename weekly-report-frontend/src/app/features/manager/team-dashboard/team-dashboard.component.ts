import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { DashboardService } from '../../../core/services/dashboard.service';
import {
  ActivityFeedItemResponse, DashboardSummaryResponse, StatusByMemberResponse,
  TasksTrendPointResponse, TimeByTaskTypeResponse, WorkloadByProjectResponse
} from '../../../core/models/dashboard.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { ChartComponent } from '../../../shared/components/chart/chart.component';

function mondayOf(date: Date): Date {
  const d = new Date(date);
  const day = d.getDay();
  d.setDate(d.getDate() + (day === 0 ? -6 : 1 - day));
  return d;
}
function toIso(d: Date): string { return d.toISOString().slice(0, 10); }

const CHART_PALETTE = ['#2D5C4D', '#2D5C8A', '#B5462F', '#8A7A2D', '#5C4D8A', '#2D8A7A', '#8A2D5C'];

@Component({
  selector: 'app-team-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, SpinnerComponent, ChartComponent],
  templateUrl: './team-dashboard.component.html'
})
export class TeamDashboardComponent implements OnInit {
  weekStart = signal(toIso(mondayOf(new Date())));
  loading = signal(true);

  summary = signal<DashboardSummaryResponse | null>(null);
  statusByMember = signal<StatusByMemberResponse[]>([]);
  workloadByProject = signal<WorkloadByProjectResponse[]>([]);
  timeByTaskType = signal<TimeByTaskTypeResponse[]>([]);
  tasksTrend = signal<TasksTrendPointResponse[]>([]);
  activityFeed = signal<ActivityFeedItemResponse[]>([]);

  constructor(private readonly dashboardService: DashboardService) {}

  ngOnInit(): void { this.load(); }

  onWeekChange(value: string): void {
    this.weekStart.set(value);
    this.load();
  }

  shiftWeek(days: number): void {
    const d = new Date(this.weekStart() + 'T00:00:00');
    d.setDate(d.getDate() + days);
    this.weekStart.set(toIso(d));
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    forkJoin({
      summary: this.dashboardService.getSummary(this.weekStart()),
      statusByMember: this.dashboardService.getStatusByMember(this.weekStart()),
      workloadByProject: this.dashboardService.getWorkloadByProject(this.weekStart()),
      timeByTaskType: this.dashboardService.getTimeByTaskType(this.weekStart()),
      tasksTrend: this.dashboardService.getTasksCompletedTrend(8),
      activityFeed: this.dashboardService.getActivityFeed(10)
    }).subscribe((res) => {
      this.summary.set(res.summary);
      this.statusByMember.set(res.statusByMember);
      this.workloadByProject.set(res.workloadByProject);
      this.timeByTaskType.set(res.timeByTaskType);
      this.tasksTrend.set(res.tasksTrend);
      this.activityFeed.set(res.activityFeed);
      this.loading.set(false);
    });
  }

  // ---- chart data getters ----

  workloadLabels(): string[] { return this.workloadByProject().map((w) => w.projectName); }
  workloadHoursDataset() {
    return [{ label: 'Hours', data: this.workloadByProject().map((w) => w.totalHours), backgroundColor: CHART_PALETTE[0] }];
  }

  timeByTypeLabels(): string[] { return this.timeByTaskType().map((t) => t.taskType.replace('_', ' ')); }
  timeByTypeDataset() {
    return [{
      label: 'Hours',
      data: this.timeByTaskType().map((t) => t.totalHours),
      backgroundColor: this.timeByTaskType().map((_, i) => CHART_PALETTE[i % CHART_PALETTE.length])
    }];
  }

  trendLabels(): string[] { return this.tasksTrend().map((p) => p.weekStartDate); }
  trendDataset() {
    return [{
      label: 'Tasks completed',
      data: this.tasksTrend().map((p) => p.tasksCompletedCount),
      borderColor: CHART_PALETTE[0],
      backgroundColor: 'transparent',
      tension: 0.3
    }];
  }
}
