import { AfterViewInit, Component, Renderer2 } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div style="position: fixed; top: 16px; right: 16px; z-index: 1000;">
      <button
        id="themeToggle"
        type="button"
        class="btn btn-text"
        aria-label="Toggle dark mode"
        title="Toggle dark mode"
        style="font-size: 20px; padding: 8px 12px; background: var(--color-surface); border-radius: var(--radius-sm);"
      >
        🌙
      </button>
    </div>
    <router-outlet></router-outlet>
  `
})
export class AppComponent implements AfterViewInit {
  constructor(private readonly renderer: Renderer2) {}

  ngAfterViewInit(): void {
    this.applyTheme();
    this.setupThemeToggle();
  }

  private applyTheme(): void {
    const savedTheme = localStorage.getItem('theme');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    if (savedTheme === 'dark' || (!savedTheme && prefersDark)) {
      this.renderer.setAttribute(document.documentElement, 'data-theme', 'dark');
    } else {
      this.renderer.setAttribute(document.documentElement, 'data-theme', 'light');
    }

    this.updateButtonIcon();
  }

  private setupThemeToggle(): void {
    const toggleButton = document.getElementById('themeToggle');

    if (!toggleButton) {
      return;
    }

    this.renderer.listen(toggleButton, 'click', () => {
      const currentTheme = document.documentElement.getAttribute('data-theme');
      const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

      if (newTheme === 'dark') {
        this.renderer.setAttribute(document.documentElement, 'data-theme', 'dark');
        localStorage.setItem('theme', 'dark');
      } else {
        this.renderer.setAttribute(document.documentElement, 'data-theme', 'light');
        localStorage.setItem('theme', 'light');
      }

      this.updateButtonIcon();
    });

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (!localStorage.getItem('theme')) {
        if (e.matches) {
          this.renderer.setAttribute(document.documentElement, 'data-theme', 'dark');
        } else {
          this.renderer.setAttribute(document.documentElement, 'data-theme', 'light');
        }
        this.updateButtonIcon();
      }
    });
  }

  private updateButtonIcon(): void {
    const button = document.getElementById('themeToggle');
    if (!button) {
      return;
    }

    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    button.innerHTML = isDark ? '☀️' : '🌙';
    button.setAttribute('aria-label', isDark ? 'Switch to light mode' : 'Switch to dark mode');
    button.setAttribute('title', isDark ? 'Switch to light mode' : 'Switch to dark mode');
  }
}
