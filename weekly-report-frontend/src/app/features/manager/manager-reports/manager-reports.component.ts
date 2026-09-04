import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { ManagerReportService } from '../../../core/services/report.service';
import { ProjectService } from '../../../core/services/project.service';
import { UserService } from '../../../core/services/user.service';
import { ReportSummaryResponse, ReportStatus } from '../../../core/models/report.model';
import { ProjectResponse } from '../../../core/models/project.model';
import { UserResponse } from '../../../core/models/user.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';

const STATUSES: ReportStatus[] = ['DRAFT', 'SUBMITTED', 'NEEDS_CORRECTION', 'APPROVED'];

@Component({
  selector: 'app-manager-reports',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, StatusBadgeComponent, PaginationComponent, SpinnerComponent],
  templateUrl: './manager-reports.component.html'
})
export class ManagerReportsComponent implements OnInit {
  readonly statuses = STATUSES;

  members = signal<UserResponse[]>([]);
  projects = signal<ProjectResponse[]>([]);
  reports = signal<ReportSummaryResponse[]>([]);
  loading = signal(true);
  page = signal(0);
  totalPages = signal(0);

  filterForm = this.fb.group({
    memberId: [null as number | null],
    projectId: [null as number | null],
    status: [null as ReportStatus | null],
    weekStart: [''],
    weekEnd: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly managerReportService: ManagerReportService,
    private readonly projectService: ProjectService,
    private readonly userService: UserService
  ) {}

  ngOnInit(): void {
    forkJoin({
      members: this.userService.list(0, 100),
      projects: this.projectService.listAll(0, 100)
    }).subscribe(({ members, projects }) => {
      this.members.set(members.content.filter((m) => m.role === 'TEAM_MEMBER'));
      this.projects.set(projects.content);
      this.search();
    });
  }

  search(): void {
    this.page.set(0);
    this.load();
  }

  resetFilters(): void {
    this.filterForm.reset({ memberId: null, projectId: null, status: null, weekStart: '', weekEnd: '' });
    this.search();
  }

  onPageChange(page: number): void {
    this.page.set(page);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    const raw = this.filterForm.getRawValue();

    this.managerReportService.search({
      memberId: raw.memberId,
      projectId: raw.projectId,
      status: raw.status,
      weekStart: raw.weekStart || null,
      weekEnd: raw.weekEnd || null
    }, this.page(), 15).subscribe((res) => {
      this.reports.set(res.content);
      this.totalPages.set(res.totalPages);
      this.loading.set(false);
    });
  }

  /** Submitted reports open in the review screen; everything else opens the read-only detail. */
  targetFor(r: ReportSummaryResponse): string[] {
    return r.status === 'SUBMITTED'
      ? ['/manager/reports', String(r.id), 'review']
      : ['/manager/reports', String(r.id)];
  }
}
