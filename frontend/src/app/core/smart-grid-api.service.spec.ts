import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL, SmartGridApiService } from './smart-grid-api.service';

describe('SmartGridApiService', () => {
  let service: SmartGridApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: 'http://api.test/api/v1' }
      ]
    });
    service = TestBed.inject(SmartGridApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('requests analytics with the selected timespan', () => {
    service.getGridLoad('6h').subscribe();

    const request = http.expectOne(
      candidate => candidate.url === 'http://api.test/api/v1/analytics/grid-load'
    );
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('timespan')).toBe('6h');
    request.flush({ loadProfile: [] });
  });

  it('posts a typed meter reading', () => {
    const payload = {
      meterId: 'AT-001',
      gridArea: 'Vienna-Center',
      consumptionKwh: 2.5,
      recordedAt: '2026-08-20T12:00:00Z'
    };
    service.ingestReading(payload).subscribe();

    const request = http.expectOne('http://api.test/api/v1/readings/ingest');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ ...payload, receivedAt: '2026-08-20T12:00:01Z' });
  });

  it('requests area and anomaly analytics with explicit parameters', () => {
    service.getGridAreas('7d').subscribe();
    service.getAnomalies('7d', 6.25).subscribe();

    const areaRequest = http.expectOne(
      candidate => candidate.url.endsWith('/analytics/grid-areas')
    );
    expect(areaRequest.request.params.get('timespan')).toBe('7d');
    areaRequest.flush({ areas: [] });

    const anomalyRequest = http.expectOne(
      candidate => candidate.url.endsWith('/analytics/anomalies')
    );
    expect(anomalyRequest.request.params.get('thresholdKwh')).toBe('6.25');
    anomalyRequest.flush([]);
  });

  it('updates a URL-encoded meter resource', () => {
    service.changeMeterStatus('AT/VIE 001', 'INACTIVE').subscribe();

    const request = http.expectOne(
      'http://api.test/api/v1/meters/AT%2FVIE%20001/status'
    );
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ status: 'INACTIVE' });
    request.flush({ meterId: 'AT/VIE 001', status: 'INACTIVE' });
  });
});
