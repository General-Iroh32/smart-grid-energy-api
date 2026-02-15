import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { EMPTY, catchError, finalize } from 'rxjs';
import { AnalyticsTimespan, GridLoadAnalytics } from '../core/api.models';
import { SmartGridApiService } from '../core/smart-grid-api.service';
import { LoadProfileChartComponent } from './load-profile-chart.component';

const TIMESPANS: readonly AnalyticsTimespan[] = ['1h', '6h', '24h', '7d'];

@Component({
  selector: 'app-grid-overview',
  standalone: true,
  imports: [DecimalPipe, LoadProfileChartComponent],
  template: `
    <section class="overview" aria-labelledby="overview-heading">
      <div class="section-heading">
        <div><p class="eyebrow">Live overview</p><h2 id="overview-heading">Grid load</h2></div>
        <div class="timespans" aria-label="Analytics period">
          @for (option of timespans; track option) {
            <button type="button" [class.active]="timespan() === option"
              [attr.aria-pressed]="timespan() === option" (click)="selectTimespan(option)">{{ option }}</button>
          }
        </div>
      </div>

      @if (loading() && !analytics()) {
        <div class="status-card loading" role="status"><span></span> Loading grid telemetry…</div>
      } @else if (error()) {
        <div class="status-card error" role="alert">
          <div><strong>Telemetry unavailable</strong><p>{{ error() }}</p></div>
          <button type="button" (click)="load()">Try again</button>
        </div>
      } @else if (analytics(); as data) {
        <div class="kpi-grid">
          <article><span>Total consumption</span><strong>{{ data.totalConsumptionKwh | number:'1.1-2' }}</strong><small>kWh / {{ data.timespan }}</small></article>
          <article><span>Peak reading</span><strong>{{ data.peakConsumptionKwh | number:'1.1-2' }}</strong><small>kWh</small></article>
          <article><span>Active meters</span><strong>{{ data.activeMeterCount }}</strong><small>{{ data.readingCount }} readings</small></article>
          <article><span>Average signal</span><strong>{{ data.averageConsumptionKwh | number:'1.1-2' }}</strong><small>kWh / reading</small></article>
        </div>
        <app-load-profile-chart [points]="data.loadProfile" />
      }
    </section>
  `,
  styleUrl: './grid-overview.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GridOverviewComponent implements OnInit {
  private readonly api = inject(SmartGridApiService);
  readonly timespans = TIMESPANS;
  readonly timespan = signal<AnalyticsTimespan>('24h');
  readonly analytics = signal<GridLoadAnalytics | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly hasData = computed(() => (this.analytics()?.readingCount ?? 0) > 0);

  ngOnInit(): void { this.load(); }

  selectTimespan(timespan: AnalyticsTimespan): void {
    if (timespan !== this.timespan()) {
      this.timespan.set(timespan);
      this.load();
    }
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.getGridLoad(this.timespan()).pipe(
      catchError(() => {
        this.error.set('The API did not return grid analytics. Check the backend connection.');
        return EMPTY;
      }),
      finalize(() => this.loading.set(false))
    ).subscribe(data => this.analytics.set(data));
  }
}
