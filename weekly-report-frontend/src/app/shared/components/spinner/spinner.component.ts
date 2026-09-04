import { Component } from '@angular/core';

@Component({
  selector: 'app-spinner',
  standalone: true,
  template: `<div class="spinner" role="status" aria-label="Loading"></div>`,
  styles: [`
    .spinner {
      width: 22px; height: 22px;
      border: 2.5px solid var(--color-border-strong);
      border-top-color: var(--color-primary);
      border-radius: 50%;
      animation: spin 700ms linear infinite;
      margin: 24px auto;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class SpinnerComponent {}
