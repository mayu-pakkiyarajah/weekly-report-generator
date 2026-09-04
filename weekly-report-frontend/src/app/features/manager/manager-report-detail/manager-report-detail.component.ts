import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { ManagerReportService } from '../../../core/services/report.service';
import { ReportResponse, ReportVersionResponse } from '../../../core/models/report.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';

@Component({
  selector: 'app-manager-report-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, SpinnerComponent],
  templateUrl: './manager-report-detail.component.html'
})
export class ManagerReportDetailComponent implements OnInit {
  reportId!: number;
  report = signal<ReportResponse | null>(null);
  versions = signal<ReportVersionResponse[]>([]);
  viewingVersion = signal<ReportVersionResponse | null>(null);
  loading = signal(true);

  constructor(private readonly route: ActivatedRoute, private readonly managerReportService: ManagerReportService) {}

  ngOnInit(): void {
    this.reportId = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin({
      report: this.managerReportService.get(this.reportId),
      versions: this.managerReportService.getVersionHistory(this.reportId)
    }).subscribe(({ report, versions }) => {
      this.report.set(report);
      this.versions.set(versions);
      this.viewingVersion.set(report.currentVersion);
      this.loading.set(false);
    });
  }

  viewVersion(v: ReportVersionResponse): void {
    this.viewingVersion.set(v);
  }

  isCurrentVersion(v: ReportVersionResponse): boolean {
    return v.versionNumber === this.report()?.currentVersion.versionNumber;
  }

  totalHours(v: ReportVersionResponse | null): number {
    return v ? v.hoursByTaskType.reduce((sum, h) => sum + h.hours, 0) : 0;
  }
}
