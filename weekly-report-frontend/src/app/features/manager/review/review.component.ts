import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { ManagerReportService } from '../../../core/services/report.service';
import { ReportResponse, ReportVersionResponse, ReviewAction } from '../../../core/models/report.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-review',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, StatusBadgeComponent, SpinnerComponent, ConfirmDialogComponent],
  templateUrl: './review.component.html'
})
export class ReviewComponent implements OnInit {
  reportId!: number;
  report = signal<ReportResponse | null>(null);
  versions = signal<ReportVersionResponse[]>([]);
  viewingVersion = signal<ReportVersionResponse | null>(null);
  loading = signal(true);
  submitting = signal(false);
  errorMessage = signal<string | null>(null);
  pendingAction = signal<ReviewAction | null>(null);

  reviewForm = this.fb.group({
    comment: ['', [Validators.required, Validators.maxLength(2000)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly managerReportService: ManagerReportService
  ) {}

  ngOnInit(): void {
    this.reportId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    forkJoin({
      report: this.managerReportService.get(this.reportId),
      versions: this.managerReportService.getVersionHistory(this.reportId)
    }).subscribe(({ report, versions }) => {
      this.report.set(report);
      this.versions.set(versions);
      this.viewingVersion.set(report.currentVersion);
      this.loading.set(false);

      if (report.status !== 'SUBMITTED') {
        // Nothing to review right now - send the manager to the read-only view instead.
        this.router.navigate(['/manager/reports', this.reportId]);
      }
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

  requestAction(action: ReviewAction): void {
    if (this.reviewForm.invalid) {
      this.reviewForm.markAllAsTouched();
      return;
    }
    this.pendingAction.set(action);
  }

  cancelAction(): void {
    this.pendingAction.set(null);
  }

  confirmAction(): void {
    const action = this.pendingAction();
    if (!action) return;

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.managerReportService.review(this.reportId, {
      action,
      comment: this.reviewForm.getRawValue().comment!
    }).subscribe({
      next: () => this.router.navigate(['/manager/reports']),
      error: (err) => {
        this.submitting.set(false);
        this.pendingAction.set(null);
        this.errorMessage.set(err?.error?.message ?? 'Could not submit the review.');
      }
    });
  }
}
