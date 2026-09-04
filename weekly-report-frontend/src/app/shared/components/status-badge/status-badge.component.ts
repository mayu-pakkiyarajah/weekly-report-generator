import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportStatus } from '../../../core/models/report.model';

const LABELS: Record<ReportStatus, string> = {
  DRAFT: 'Draft',
  SUBMITTED: 'Submitted',
  NEEDS_CORRECTION: 'Needs correction',
  APPROVED: 'Approved'
};

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="badge" [ngClass]="'badge-' + (status ?? 'NONE').toLowerCase()">
      {{ status ? labels[status] : 'Not started' }}
    </span>
  `,
  styles: [`
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 3px 10px 3px 8px;
      border-radius: 3px;
      font-size: 12px;
      font-weight: 500;
      border-left: 3px solid currentColor;
    }
    .badge-draft { background: var(--color-status-draft-soft); color: var(--color-status-draft); }
    .badge-submitted { background: var(--color-status-submitted-soft); color: var(--color-status-submitted); }
    .badge-needs_correction { background: var(--color-status-correction-soft); color: var(--color-status-correction); }
    .badge-approved { background: var(--color-status-approved-soft); color: var(--color-status-approved); }
    .badge-none { background: var(--color-bg); color: var(--color-muted); border-left-color: var(--color-border-strong); }
  `]
})
export class StatusBadgeComponent {
  @Input() status: ReportStatus | null = null;
  readonly labels = LABELS;
}
