import { DatePipe, DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  inject,
  signal
} from '@angular/core';
import { EMPTY, catchError, finalize, forkJoin } from 'rxjs';
import {
  AnalyticsTimespan,
  GridAreaAnalytics,
  LoadAnomaly,
  MeterStatus,
  MeterSummary
} from '../core/api.models';
import { SmartGridApiService } from '../core/smart-grid-api.service';

@Component({
  selector: 'app-operations-panel',
  standalone: true,
  imports: [DatePipe, DecimalPipe],
  template: `
    <section class="operations" aria-labelledby="operations-heading">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Operational intelligence</p>
          <h2 id="operations-heading">Area health & fleet</h2>
        </div>
        <button class="refresh" type="button" (click)="load()" [disabled]="loading()">
          {{ loading() ? 'Refreshing…' : 'Refresh data' }}
        </button>
      </div>

      @if (error()) {
        <p class="error" role="alert">{{ error() }}</p>
      }

      <div class="area-grid">
        @for (area of areaAnalytics()?.areas ?? []; track area.gridArea) {
          <article class="area-card">
            <div class="area-title">
              <div><span>Grid area</span><h3>{{ area.gridArea }}</h3></div>
              <strong [class]="'state ' + area.operationalState.toLowerCase()">
                {{ area.operationalState }}
              </strong>
            </div>
            <div class="share-track" aria-hidden="true">
              <span [style.width.%]="area.loadSharePercent"></span>
            </div>
            <dl>
              <div><dt>Load share</dt><dd>{{ area.loadSharePercent | number:'1.0-1' }}%</dd></div>
              <div><dt>Total</dt><dd>{{ area.totalConsumptionKwh | number:'1.1-2' }} kWh</dd></div>
              <div><dt>Peak</dt><dd>{{ area.peakConsumptionKwh | number:'1.1-2' }} kWh</dd></div>
              <div><dt>Meters</dt><dd>{{ area.activeMeterCount }}</dd></div>
            </dl>
          </article>
        } @empty {
          <article class="empty">No grid-area signals are available for this period.</article>
        }
      </div>

      <div class="operations-grid">
        <article class="panel alert-panel">
          <div class="panel-heading">
            <div><span>Threshold monitor</span><h3>Load anomalies</h3></div>
            <strong>{{ anomalies().length }}</strong>
          </div>
          <div class="alert-list">
            @for (anomaly of anomalies(); track anomaly.meterId + anomaly.recordedAt) {
              <div class="alert-row">
                <span class="alert-icon" aria-hidden="true">!</span>
                <div><strong>{{ anomaly.meterId }}</strong><small>{{ anomaly.gridArea }} · {{ anomaly.recordedAt | date:'shortTime':'UTC' }} UTC</small></div>
                <div class="alert-value"><strong>{{ anomaly.consumptionKwh | number:'1.1-2' }}</strong><small>+{{ anomaly.excessPercent | number:'1.0-0' }}%</small></div>
              </div>
            } @empty {
              <p class="empty-inline">No readings exceeded 4.5 kWh.</p>
            }
          </div>
        </article>

        <article class="panel fleet-panel">
          <div class="panel-heading">
            <div><span>Asset registry</span><h3>Meter fleet</h3></div>
            <strong>{{ meters().length }}</strong>
          </div>
          <div class="fleet-table" role="table" aria-label="Smart meter fleet">
            @for (meter of meters(); track meter.meterId) {
              <div class="meter-row" role="row">
                <div role="cell"><strong>{{ meter.meterId }}</strong><small>{{ meter.gridArea }}</small></div>
                <div role="cell"><strong>{{ meter.readingCount }}</strong><small>signals</small></div>
                <button type="button" [class.inactive]="meter.status === 'INACTIVE'"
                  [disabled]="updatingMeter() === meter.meterId"
                  (click)="toggleMeter(meter)">
                  {{ updatingMeter() === meter.meterId ? 'Saving…' : meter.status }}
                </button>
              </div>
            } @empty {
              <p class="empty-inline">No meters are registered.</p>
            }
          </div>
        </article>
      </div>
    </section>
  `,
  styleUrl: './operations-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OperationsPanelComponent implements OnChanges, OnInit {
  private readonly api = inject(SmartGridApiService);

  @Input() timespan: AnalyticsTimespan = '24h';

  readonly areaAnalytics = signal<GridAreaAnalytics | null>(null);
  readonly anomalies = signal<readonly LoadAnomaly[]>([]);
  readonly meters = signal<readonly MeterSummary[]>([]);
  readonly loading = signal(false);
  readonly updatingMeter = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['timespan'] && !changes['timespan'].firstChange) {
      this.load();
    }
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      areas: this.api.getGridAreas(this.timespan),
      anomalies: this.api.getAnomalies(this.timespan),
      meters: this.api.getMeters()
    }).pipe(
      catchError(() => {
        this.error.set('Operational data could not be refreshed.');
        return EMPTY;
      }),
      finalize(() => this.loading.set(false))
    ).subscribe(result => {
      this.areaAnalytics.set(result.areas);
      this.anomalies.set(result.anomalies);
      this.meters.set(result.meters);
    });
  }

  toggleMeter(meter: MeterSummary): void {
    const nextStatus: MeterStatus = meter.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.error.set(null);
    this.updatingMeter.set(meter.meterId);
    this.api.changeMeterStatus(meter.meterId, nextStatus).pipe(
      catchError(() => {
        this.error.set(`Status for ${meter.meterId} could not be changed.`);
        return EMPTY;
      }),
      finalize(() => this.updatingMeter.set(null))
    ).subscribe(updated => this.meters.update(meters =>
      meters.map(candidate => candidate.meterId === updated.meterId ? updated : candidate)
    ));
  }
}
