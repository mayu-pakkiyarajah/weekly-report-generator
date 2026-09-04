import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Small inline confirmation panel (not a native browser confirm()) used before
 * destructive or hard-to-reverse actions: deleting a project, approving/requesting
 * changes on a report, deactivating a user.
 */
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="overlay" *ngIf="open" (click)="cancel.emit()">
      <div class="panel card" (click)="$event.stopPropagation()">
        <h3>{{ title }}</h3>
        <p>{{ message }}</p>
        <div class="row" style="justify-content: flex-end; margin-top: 16px;">
          <button class="btn btn-text" (click)="cancel.emit()">Cancel</button>
          <button class="btn" [class.btn-danger]="danger" [class.btn-primary]="!danger" (click)="confirm.emit()">
            {{ confirmLabel }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .overlay {
      position: fixed; inset: 0; background: rgba(27, 31, 35, 0.35);
      display: flex; align-items: center; justify-content: center; z-index: 100;
    }
    .panel { width: 360px; max-width: 90vw; }
  `]
})
export class ConfirmDialogComponent {
  @Input() open = false;
  @Input() title = 'Are you sure?';
  @Input() message = 'This action cannot be undone.';
  @Input() confirmLabel = 'Confirm';
  @Input() danger = false;
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}
