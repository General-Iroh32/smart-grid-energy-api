import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { GridLoadPoint } from '../core/api.models';

@Component({
  selector: 'app-load-profile-chart',
  standalone: true,
  template: `
    <article class="chart-card" aria-labelledby="load-profile-heading">
      <div class="chart-heading">
        <div><p>Consumption profile</p><h3 id="load-profile-heading">Demand over time</h3></div>
        <span>{{ points().length }} intervals</span>
      </div>
      @if (points().length === 0) {
        <div class="empty-state">
          <strong>No readings in this period</strong>
          <p>Ingest a meter reading or select a wider timespan.</p>
        </div>
      } @else {
        <div class="chart-wrap">
          <svg viewBox="0 0 1000 300" role="img" aria-labelledby="chart-title chart-description"
            preserveAspectRatio="none">
            <title id="chart-title">Grid consumption profile</title>
            <desc id="chart-description">Consumption in kilowatt hours across {{ points().length }} time intervals.</desc>
            <defs>
              <linearGradient id="area-fill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#95f45b" stop-opacity=".32" />
                <stop offset="1" stop-color="#95f45b" stop-opacity="0" />
              </linearGradient>
            </defs>
            @for (line of gridLines; track line) {
              <line x1="0" [attr.y1]="line" x2="1000" [attr.y2]="line" class="grid-line" />
            }
            <polygon [attr.points]="areaPoints()" fill="url(#area-fill)" />
            <polyline [attr.points]="linePoints()" class="load-line" />
          </svg>
          <div class="axis"><span>{{ firstLabel() }}</span><span>{{ lastLabel() }}</span></div>
        </div>
      }
    </article>
  `,
  styleUrl: './load-profile-chart.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadProfileChartComponent {
  readonly points = input.required<readonly GridLoadPoint[]>();
  readonly gridLines = [20, 85, 150, 215, 280];
  readonly linePoints = computed(() => this.scalePoints().map(point => `${point.x},${point.y}`).join(' '));
  readonly areaPoints = computed(() => `0,300 ${this.linePoints()} 1000,300`);
  readonly firstLabel = computed(() => this.formatTimestamp(this.points().at(0)?.timestamp));
  readonly lastLabel = computed(() => this.formatTimestamp(this.points().at(-1)?.timestamp));

  private scalePoints(): Array<{ x: number; y: number }> {
    const values = this.points();
    const max = Math.max(...values.map(point => point.consumptionKwh), 1);
    return values.map((point, index) => ({
      x: values.length === 1 ? 500 : index * (1000 / (values.length - 1)),
      y: 280 - (point.consumptionKwh / max) * 240
    }));
  }

  private formatTimestamp(value: string | undefined): string {
    if (!value) return '';
    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
    }).format(new Date(value));
  }
}

