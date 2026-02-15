import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoadProfileChartComponent } from './load-profile-chart.component';

describe('LoadProfileChartComponent', () => {
  let fixture: ComponentFixture<LoadProfileChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [LoadProfileChartComponent] }).compileComponents();
    fixture = TestBed.createComponent(LoadProfileChartComponent);
  });

  it('renders an SVG profile for readings', () => {
    fixture.componentRef.setInput('points', [
      { timestamp: '2026-08-20T10:00:00Z', consumptionKwh: 2 },
      { timestamp: '2026-08-20T11:00:00Z', consumptionKwh: 4 }
    ]);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('polyline').getAttribute('points')).toContain('1000,40');
  });

  it('shows a useful empty state', () => {
    fixture.componentRef.setInput('points', []);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No readings in this period');
  });
});

