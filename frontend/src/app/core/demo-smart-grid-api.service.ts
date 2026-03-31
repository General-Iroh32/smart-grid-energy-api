import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import {
  AnalyticsTimespan,
  GridAreaAnalytics,
  GridAreaLoad,
  GridLoadAnalytics,
  GridLoadPoint,
  IngestReadingRequest,
  IngestReadingResponse,
  LoadAnomaly,
  MeterStatus,
  MeterSummary,
  OperationalState
} from './api.models';

interface DemoMeter {
  readonly gridArea: string;
  readonly createdAt: string;
  status: MeterStatus;
}

interface DemoReading extends IngestReadingRequest {
  readonly receivedAt: string;
}

const TIMESPAN_MILLISECONDS: Readonly<Record<AnalyticsTimespan, number>> = {
  '1h': 60 * 60 * 1000,
  '6h': 6 * 60 * 60 * 1000,
  '24h': 24 * 60 * 60 * 1000,
  '7d': 7 * 24 * 60 * 60 * 1000
};

@Injectable()
export class DemoSmartGridApiService {
  private readonly meters = new Map<string, DemoMeter>([
    ['AT-DEMO-1001', this.meter('Vienna-Center', 'ACTIVE', 180)],
    ['AT-DEMO-2001', this.meter('Vienna-West', 'ACTIVE', 120)],
    ['AT-DEMO-3001', this.meter('Danube-North', 'ACTIVE', 75)]
  ]);
  private readonly readings: DemoReading[] = this.seedReadings();

  getGridLoad(timespan: AnalyticsTimespan): Observable<GridLoadAnalytics> {
    const { from, to, readings } = this.window(timespan);
    const total = readings.reduce((sum, reading) => sum + reading.consumptionKwh, 0);
    const activeMeters = new Set(
      readings
        .map((reading) => reading.meterId)
        .filter((meterId) => this.meters.get(meterId)?.status === 'ACTIVE')
    );

    return of({
      timespan,
      from: from.toISOString(),
      to: to.toISOString(),
      totalConsumptionKwh: round(total),
      averageConsumptionKwh: round(readings.length === 0 ? 0 : total / readings.length),
      peakConsumptionKwh: round(Math.max(0, ...readings.map((reading) => reading.consumptionKwh))),
      readingCount: readings.length,
      activeMeterCount: activeMeters.size,
      loadProfile: this.loadProfile(readings)
    });
  }

  getGridAreas(timespan: AnalyticsTimespan): Observable<GridAreaAnalytics> {
    const { from, to, readings } = this.window(timespan);
    const total = readings.reduce((sum, reading) => sum + reading.consumptionKwh, 0);
    const byArea = groupBy(readings, (reading) => reading.gridArea);
    const areas = [...byArea.entries()]
      .map(([gridArea, areaReadings]) => this.areaSummary(gridArea, areaReadings, total))
      .sort((left, right) => right.totalConsumptionKwh - left.totalConsumptionKwh);

    return of({
      timespan,
      from: from.toISOString(),
      to: to.toISOString(),
      totalConsumptionKwh: round(total),
      areas
    });
  }

  getAnomalies(
    timespan: AnalyticsTimespan,
    thresholdKwh = 4.5
  ): Observable<readonly LoadAnomaly[]> {
    const anomalies = this.window(timespan).readings
      .filter((reading) => reading.consumptionKwh > thresholdKwh)
      .map((reading) => ({
        meterId: reading.meterId,
        gridArea: reading.gridArea,
        recordedAt: reading.recordedAt,
        consumptionKwh: reading.consumptionKwh,
        thresholdKwh,
        excessPercent: round(((reading.consumptionKwh - thresholdKwh) / thresholdKwh) * 100)
      }))
      .sort((left, right) => right.recordedAt.localeCompare(left.recordedAt));
    return of(anomalies);
  }

  getMeters(): Observable<readonly MeterSummary[]> {
    return of(
      [...this.meters.keys()]
        .sort()
        .map((meterId) => this.meterSummary(meterId))
    );
  }

  ingestReading(request: IngestReadingRequest): Observable<IngestReadingResponse> {
    const duplicate = this.readings.some(
      (reading) => reading.meterId === request.meterId && reading.recordedAt === request.recordedAt
    );
    if (duplicate) {
      return throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: {
              title: 'Duplicate meter reading',
              detail: `A reading already exists for meter ${request.meterId} at this timestamp`
            }
          })
      );
    }

    const receivedAt = new Date().toISOString();
    if (!this.meters.has(request.meterId)) {
      this.meters.set(request.meterId, {
        gridArea: request.gridArea,
        status: 'ACTIVE',
        createdAt: receivedAt
      });
    }
    this.readings.push({ ...request, receivedAt });
    return of({ ...request, receivedAt });
  }

  changeMeterStatus(meterId: string, status: MeterStatus): Observable<MeterSummary> {
    const meter = this.meters.get(meterId);
    if (!meter) {
      return throwError(() => new HttpErrorResponse({ status: 404 }));
    }
    meter.status = status;
    return of(this.meterSummary(meterId));
  }

  private meter(gridArea: string, status: MeterStatus, ageDays: number): DemoMeter {
    return {
      gridArea,
      status,
      createdAt: new Date(Date.now() - ageDays * 24 * 60 * 60 * 1000).toISOString()
    };
  }

  private seedReadings(): DemoReading[] {
    const definitions = [
      { meterId: 'AT-DEMO-1001', gridArea: 'Vienna-Center', base: 2.35 },
      { meterId: 'AT-DEMO-2001', gridArea: 'Vienna-West', base: 1.75 },
      { meterId: 'AT-DEMO-3001', gridArea: 'Danube-North', base: 1.25 }
    ] as const;
    const readings: DemoReading[] = [];
    const now = Date.now();

    for (let hour = 0; hour < 7 * 24; hour += 1) {
      for (const [index, definition] of definitions.entries()) {
        const recordedAt = new Date(now - hour * 60 * 60 * 1000).toISOString();
        const cycle = ((hour + index * 2) % 8) * 0.11;
        const peak = hour === 2 && index === 0 ? 3.45 : 0;
        readings.push({
          meterId: definition.meterId,
          gridArea: definition.gridArea,
          consumptionKwh: round(definition.base + cycle + peak),
          recordedAt,
          receivedAt: recordedAt
        });
      }
    }
    return readings;
  }

  private window(timespan: AnalyticsTimespan): {
    readonly from: Date;
    readonly to: Date;
    readonly readings: readonly DemoReading[];
  } {
    const to = new Date();
    const from = new Date(to.getTime() - TIMESPAN_MILLISECONDS[timespan]);
    return {
      from,
      to,
      readings: this.readings.filter((reading) => {
        const timestamp = Date.parse(reading.recordedAt);
        return timestamp >= from.getTime() && timestamp <= to.getTime();
      })
    };
  }

  private loadProfile(readings: readonly DemoReading[]): readonly GridLoadPoint[] {
    const buckets = groupBy(readings, (reading) => reading.recordedAt.slice(0, 13));
    return [...buckets.entries()]
      .map(([hour, bucket]) => ({
        timestamp: `${hour}:00:00.000Z`,
        consumptionKwh: round(bucket.reduce((sum, reading) => sum + reading.consumptionKwh, 0))
      }))
      .sort((left, right) => left.timestamp.localeCompare(right.timestamp));
  }

  private areaSummary(
    gridArea: string,
    readings: readonly DemoReading[],
    gridTotal: number
  ): GridAreaLoad {
    const total = readings.reduce((sum, reading) => sum + reading.consumptionKwh, 0);
    const average = readings.length === 0 ? 0 : total / readings.length;
    const peak = Math.max(0, ...readings.map((reading) => reading.consumptionKwh));
    const activeMeters = new Set(
      readings
        .map((reading) => reading.meterId)
        .filter((meterId) => this.meters.get(meterId)?.status === 'ACTIVE')
    );
    return {
      gridArea,
      totalConsumptionKwh: round(total),
      averageConsumptionKwh: round(average),
      peakConsumptionKwh: round(peak),
      readingCount: readings.length,
      activeMeterCount: activeMeters.size,
      loadSharePercent: round(gridTotal === 0 ? 0 : (total / gridTotal) * 100),
      operationalState: this.operationalState(peak, average)
    };
  }

  private operationalState(peak: number, average: number): OperationalState {
    const ratio = average === 0 ? 0 : peak / average;
    if (ratio >= 2) return 'CRITICAL';
    if (ratio >= 1.5) return 'ELEVATED';
    return 'NORMAL';
  }

  private meterSummary(meterId: string): MeterSummary {
    const meter = this.meters.get(meterId);
    if (!meter) throw new Error(`Unknown demo meter ${meterId}`);
    const readings = this.readings.filter((reading) => reading.meterId === meterId);
    const lastReadingAt = readings
      .map((reading) => reading.recordedAt)
      .sort()
      .at(-1) ?? null;
    return {
      meterId,
      gridArea: meter.gridArea,
      status: meter.status,
      createdAt: meter.createdAt,
      readingCount: readings.length,
      lastReadingAt
    };
  }
}

function round(value: number): number {
  return Number(value.toFixed(4));
}

function groupBy<T, K>(values: readonly T[], keyFor: (value: T) => K): Map<K, T[]> {
  const groups = new Map<K, T[]>();
  for (const value of values) {
    const key = keyFor(value);
    const group = groups.get(key) ?? [];
    group.push(value);
    groups.set(key, group);
  }
  return groups;
}
