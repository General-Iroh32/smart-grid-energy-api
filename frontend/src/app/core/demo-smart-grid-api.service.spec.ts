import { firstValueFrom } from 'rxjs';
import { DemoSmartGridApiService } from './demo-smart-grid-api.service';

describe('DemoSmartGridApiService', () => {
  let service: DemoSmartGridApiService;

  beforeEach(() => {
    service = new DemoSmartGridApiService();
  });

  it('produces bounded analytics for every dashboard period', async () => {
    const analytics = await firstValueFrom(service.getGridLoad('24h'));

    expect(analytics.timespan).toBe('24h');
    expect(analytics.readingCount).toBeGreaterThan(0);
    expect(analytics.activeMeterCount).toBe(3);
    expect(analytics.loadProfile.length).toBeGreaterThan(0);
  });

  it('keeps ingestion and fleet state interactive in memory', async () => {
    await firstValueFrom(
      service.ingestReading({
        meterId: 'AT-DEMO-E2E',
        gridArea: 'Vienna-Lab',
        consumptionKwh: 3.75,
        recordedAt: new Date(Date.now() - 1000).toISOString()
      })
    );
    const updated = await firstValueFrom(service.changeMeterStatus('AT-DEMO-E2E', 'INACTIVE'));
    const meters = await firstValueFrom(service.getMeters());

    expect(updated.status).toBe('INACTIVE');
    expect(meters.some((meter) => meter.meterId === 'AT-DEMO-E2E')).toBe(true);
  });

  it('rejects duplicate meter timestamps like the backend contract', async () => {
    const request = {
      meterId: 'AT-DEMO-E2E',
      gridArea: 'Vienna-Lab',
      consumptionKwh: 2,
      recordedAt: new Date(Date.now() - 1000).toISOString()
    };
    await firstValueFrom(service.ingestReading(request));

    await expect(firstValueFrom(service.ingestReading(request))).rejects.toMatchObject({ status: 409 });
  });
});
