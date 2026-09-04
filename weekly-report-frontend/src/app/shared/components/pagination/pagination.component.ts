import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pagination" *ngIf="totalPages > 1">
      <button class="btn btn-text" [disabled]="page === 0" (click)="pageChange.emit(page - 1)">Previous</button>
      <span class="page-info">Page {{ page + 1 }} of {{ totalPages }}</span>
      <button class="btn btn-text" [disabled]="page >= totalPages - 1" (click)="pageChange.emit(page + 1)">Next</button>
    </div>
  `,
  styles: [`
    .pagination { display: flex; align-items: center; gap: 12px; justify-content: flex-end; margin-top: 12px; }
    .page-info { color: var(--color-muted); font-size: 13px; }
  `]
})
export class PaginationComponent {
  @Input() page = 0;
  @Input() totalPages = 0;
  @Output() pageChange = new EventEmitter<number>();
}
