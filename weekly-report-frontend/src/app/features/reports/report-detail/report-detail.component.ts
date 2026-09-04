import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReportService } from '../../../core/services/report.service';
import { ReportResponse } from '../../../core/models/report.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';

@Component({
  selector: 'app-report-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, SpinnerComponent],
  templateUrl: './report-detail.component.html'
})
export class ReportDetailComponent implements OnInit {
  report = signal<ReportResponse | null>(null);
  loading = signal(true);

  constructor(private readonly route: ActivatedRoute, private readonly reportService: ReportService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.reportService.getOwn(id).subscribe((r) => {
      this.report.set(r);
      this.loading.set(false);
    });
  }

  isEditable(status: string): boolean {
    return status === 'DRAFT' || status === 'NEEDS_CORRECTION';
  }

  totalHours(): number {
    const v = this.report()?.currentVersion;
    if (!v) return 0;
    return v.hoursByTaskType.reduce((sum, h) => sum + h.hours, 0);
  }
}
