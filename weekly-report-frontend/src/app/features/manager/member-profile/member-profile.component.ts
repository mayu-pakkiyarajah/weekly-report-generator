import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { ManagerReportService } from '../../../core/services/report.service';
import { UserService } from '../../../core/services/user.service';
import { ReportSummaryResponse } from '../../../core/models/report.model';
import { UserResponse } from '../../../core/models/user.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';

@Component({
  selector: 'app-member-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, SpinnerComponent],
  templateUrl: './member-profile.component.html'
})
export class MemberProfileComponent implements OnInit {
  member = signal<UserResponse | null>(null);
  reports = signal<ReportSummaryResponse[]>([]);
  loading = signal(true);

  approvedCount = computed(() => this.reports().filter((r) => r.status === 'APPROVED').length);
  needsCorrectionCount = computed(() => this.reports().filter((r) => r.status === 'NEEDS_CORRECTION').length);
  openBlockerReports = computed(() => this.reports().filter((r) => r.hasOpenBlockers).length);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly userService: UserService,
    private readonly managerReportService: ManagerReportService
  ) {}

  ngOnInit(): void {
    const userId = Number(this.route.snapshot.paramMap.get('id'));

    forkJoin({
      member: this.userService.getById(userId),
      reports: this.managerReportService.search({ memberId: userId }, 0, 100)
    }).subscribe(({ member, reports }) => {
      this.member.set(member);
      this.reports.set(reports.content);
      this.loading.set(false);
    });
  }

  targetFor(r: ReportSummaryResponse): string[] {
    return r.status === 'SUBMITTED'
      ? ['/manager/reports', String(r.id), 'review']
      : ['/manager/reports', String(r.id)];
  }
}
