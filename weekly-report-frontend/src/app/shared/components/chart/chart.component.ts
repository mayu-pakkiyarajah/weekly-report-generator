import { AfterViewInit, Component, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';

Chart.register(...registerables);

/** Thin wrapper around Chart.js so feature components stay declarative. */
@Component({
  selector: 'app-chart',
  standalone: true,
  template: `<div class="chart-wrap"><canvas #canvas></canvas></div>`,
  styles: [`.chart-wrap { position: relative; height: 260px; }`]
})
export class ChartComponent implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('canvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;

  @Input() type: ChartType = 'bar';
  @Input() labels: string[] = [];
  @Input() datasets: ChartConfiguration['data']['datasets'] = [];
  @Input() options: ChartConfiguration['options'] = {};

  private chart: Chart | null = null;

  ngAfterViewInit(): void { this.render(); }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['labels']?.firstChange && !changes['datasets']?.firstChange) {
      this.render();
    }
  }

  ngOnDestroy(): void { this.chart?.destroy(); }

  private render(): void {
    if (!this.canvasRef) return;
    this.chart?.destroy();
    this.chart = new Chart(this.canvasRef.nativeElement, {
      type: this.type,
      data: { labels: this.labels, datasets: this.datasets },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: (this.datasets?.length ?? 0) > 1 } },
        ...this.options
      }
    });
  }
}
