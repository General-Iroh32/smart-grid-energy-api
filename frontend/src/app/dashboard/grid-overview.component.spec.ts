import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { SmartGridApiService } from '../core/smart-grid-api.service';
import { GridOverviewComponent } from './grid-overview.component';

describe('GridOverviewComponent', () => {
  it('renders returned energy KPIs', async () => {
    const api = {
      getGridLoad: jasmine.createSpy().and.returnValue(of({
        timespan: '24h', from: '', to: '', totalConsumptionKwh: 42.5,
        averageConsumptionKwh: 2.5, peakConsumptionKwh: 6.8,
        readingCount: 17, activeMeterCount: 4, loadProfile: []
      })),
      ingestReading: jasmine.createSpy()
    };
    await TestBed.configureTestingModule({
      imports: [GridOverviewComponent],
      providers: [{ provide: SmartGridApiService, useValue: api }]
    }).compileComponents();

    const fixture = TestBed.createComponent(GridOverviewComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('42.5');
    expect(fixture.nativeElement.textContent).toContain('17 readings');
    expect(api.getGridLoad).toHaveBeenCalledWith('24h');
  });
});
