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
});
