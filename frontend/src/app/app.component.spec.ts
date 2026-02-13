import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { SmartGridApiService } from './core/smart-grid-api.service';

describe('AppComponent', () => {
  it('renders the operations console heading', async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [{
        provide: SmartGridApiService,
        useValue: { getGridLoad: () => of({ timespan: '24h', readingCount: 0, loadProfile: [] }) }
      }]
    }).compileComponents();
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('h1').textContent).toContain('Grid signals');
  });
});
