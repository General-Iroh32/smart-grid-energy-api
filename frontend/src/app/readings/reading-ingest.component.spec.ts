import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { IngestReadingRequest } from '../core/api.models';
import { SmartGridApiService } from '../core/smart-grid-api.service';
import { ReadingIngestComponent } from './reading-ingest.component';

describe('ReadingIngestComponent', () => {
  it('validates and submits a meter reading', async () => {
    const api = { ingestReading: vi.fn().mockImplementation((request: IngestReadingRequest) => of({
      ...request, receivedAt: '2026-08-20T12:00:01Z'
    })) };
    await TestBed.configureTestingModule({
      imports: [ReadingIngestComponent],
      providers: [{ provide: SmartGridApiService, useValue: api }]
    }).compileComponents();
    const fixture = TestBed.createComponent(ReadingIngestComponent);
    fixture.componentInstance.form.setValue({
      meterId: 'AT-001', gridArea: 'Vienna-Center', consumptionKwh: 3.5,
      recordedAt: '2026-08-20T12:00'
    });
    fixture.detectChanges();

    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    expect(api.ingestReading).toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Reading accepted for AT-001');
  });
});
