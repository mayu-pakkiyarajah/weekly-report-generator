import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReportService } from '../../../core/services/report.service';
import { ReportSummaryResponse } from '../../../core/models/report.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';

@Component({
  selector: 'app-report-history',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, PaginationComponent, SpinnerComponent],
  templateUrl: './report-history.component.html'
})
export class ReportHistoryComponent implements OnInit {
  reports = signal<ReportSummaryResponse[]>([]);
  loading = signal(true);
  page = signal(0);
  totalPages = signal(0);

  constructor(private readonly reportService: ReportService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.reportService.listOwn(this.page(), 10).subscribe((res) => {
      this.reports.set(res.content);
      this.totalPages.set(res.totalPages);
      this.loading.set(false);
    });
  }

  onPageChange(page: number): void {
    this.page.set(page);
    this.load();
  }

  /** Draft or needs-correction reports open in the editor; everything else opens the read-only view. */
  editableTarget(r: ReportSummaryResponse): string[] {
    return r.status === 'DRAFT' || r.status === 'NEEDS_CORRECTION'
      ? ['/reports', String(r.id), 'edit']
      : ['/reports', String(r.id)];
  }
}
