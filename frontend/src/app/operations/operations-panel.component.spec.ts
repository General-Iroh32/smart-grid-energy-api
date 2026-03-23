import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { SmartGridApiService } from '../core/smart-grid-api.service';
import { OperationsPanelComponent } from './operations-panel.component';

describe('OperationsPanelComponent', () => {
  it('renders area intelligence, anomalies and the meter fleet', async () => {
    const meter = {
      meterId: 'AT-VIE-1001', gridArea: 'Vienna-Center', status: 'ACTIVE' as const,
      createdAt: '2026-08-20T12:00:00Z', readingCount: 24,
      lastReadingAt: '2026-08-21T08:00:00Z'
    };
    const api = {
      getGridAreas: vi.fn().mockReturnValue(of({
        timespan: '24h', from: '', to: '', totalConsumptionKwh: 80,
        areas: [{ gridArea: 'Vienna-Center', totalConsumptionKwh: 60,
          averageConsumptionKwh: 2.5, peakConsumptionKwh: 6,
          readingCount: 24, activeMeterCount: 1, loadSharePercent: 75,
          operationalState: 'ELEVATED' }]
      })),
      getAnomalies: vi.fn().mockReturnValue(of([{ meterId: meter.meterId,
        gridArea: meter.gridArea, recordedAt: meter.lastReadingAt,
        consumptionKwh: 6, thresholdKwh: 4.5, excessPercent: 33.33 }])),
      getMeters: vi.fn().mockReturnValue(of([meter])),
      changeMeterStatus: vi.fn().mockReturnValue(of({ ...meter, status: 'INACTIVE' }))
    };
    await TestBed.configureTestingModule({
      imports: [OperationsPanelComponent],
      providers: [{ provide: SmartGridApiService, useValue: api }]
    }).compileComponents();

    const fixture = TestBed.createComponent(OperationsPanelComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Vienna-Center');
    expect(fixture.nativeElement.textContent).toContain('ELEVATED');
    expect(fixture.nativeElement.textContent).toContain('AT-VIE-1001');
    expect(api.getGridAreas).toHaveBeenCalledWith('24h');

    fixture.componentInstance.error.set('Previous request failed.');
    fixture.nativeElement.querySelector('.meter-row button').click();
    fixture.detectChanges();
    expect(api.changeMeterStatus).toHaveBeenCalledWith('AT-VIE-1001', 'INACTIVE');
    expect(fixture.componentInstance.error()).toBeNull();
  });
});
