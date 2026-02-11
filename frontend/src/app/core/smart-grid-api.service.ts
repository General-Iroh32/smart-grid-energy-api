import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable, InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AnalyticsTimespan,
  GridLoadAnalytics,
  IngestReadingRequest,
  IngestReadingResponse
} from './api.models';

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => '/api/v1'
});

@Injectable({ providedIn: 'root' })
export class SmartGridApiService {
  constructor(
    private readonly http: HttpClient,
    @Inject(API_BASE_URL) private readonly baseUrl: string
  ) {}

  getGridLoad(timespan: AnalyticsTimespan): Observable<GridLoadAnalytics> {
    const params = new HttpParams().set('timespan', timespan);
    return this.http.get<GridLoadAnalytics>(`${this.baseUrl}/analytics/grid-load`, { params });
  }

  ingestReading(request: IngestReadingRequest): Observable<IngestReadingResponse> {
    return this.http.post<IngestReadingResponse>(`${this.baseUrl}/readings/ingest`, request);
  }
}

