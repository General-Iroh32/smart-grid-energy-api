import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, InjectionToken, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AnalyticsTimespan,
  GridAreaAnalytics,
  GridLoadAnalytics,
  IngestReadingRequest,
  IngestReadingResponse,
  LoadAnomaly,
  MeterStatus,
  MeterSummary
} from './api.models';

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => '/api/v1'
});

@Injectable({ providedIn: 'root' })
export class SmartGridApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getGridLoad(timespan: AnalyticsTimespan): Observable<GridLoadAnalytics> {
    const params = new HttpParams().set('timespan', timespan);
    return this.http.get<GridLoadAnalytics>(`${this.baseUrl}/analytics/grid-load`, { params });
  }

  ingestReading(request: IngestReadingRequest): Observable<IngestReadingResponse> {
    return this.http.post<IngestReadingResponse>(`${this.baseUrl}/readings/ingest`, request);
  }

  getGridAreas(timespan: AnalyticsTimespan): Observable<GridAreaAnalytics> {
    const params = new HttpParams().set('timespan', timespan);
    return this.http.get<GridAreaAnalytics>(`${this.baseUrl}/analytics/grid-areas`, { params });
  }

  getAnomalies(timespan: AnalyticsTimespan, thresholdKwh = 4.5): Observable<readonly LoadAnomaly[]> {
    const params = new HttpParams()
      .set('timespan', timespan)
      .set('thresholdKwh', thresholdKwh);
    return this.http.get<readonly LoadAnomaly[]>(`${this.baseUrl}/analytics/anomalies`, { params });
  }

  getMeters(): Observable<readonly MeterSummary[]> {
    return this.http.get<readonly MeterSummary[]>(`${this.baseUrl}/meters`);
  }

  changeMeterStatus(meterId: string, status: MeterStatus): Observable<MeterSummary> {
    return this.http.patch<MeterSummary>(
      `${this.baseUrl}/meters/${encodeURIComponent(meterId)}/status`,
      { status }
    );
  }
}
