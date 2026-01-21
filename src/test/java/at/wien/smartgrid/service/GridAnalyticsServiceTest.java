package at.wien.smartgrid.service;

import at.wien.smartgrid.model.entity.MeterReading;
import at.wien.smartgrid.model.entity.SmartMeter;
import at.wien.smartgrid.repository.MeterReadingRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GridAnalyticsServiceTest {

    @Test
    void aggregatesTotalsMetersAndHourlyLoad() {
        MeterReadingRepository repository = mock(MeterReadingRepository.class);
        Instant now = Instant.parse("2026-08-20T14:30:00Z");
        SmartMeter meterA = new SmartMeter("AT-001", "Center");
        SmartMeter meterB = new SmartMeter("AT-002", "West");
        when(repository.findAllByRecordedAtGreaterThanEqualAndRecordedAtLessThan(any(), any()))
                .thenReturn(List.of(
                        new MeterReading(meterA, new BigDecimal("2.0"), now.minusSeconds(1800)),
                        new MeterReading(meterB, new BigDecimal("4.0"), now.minusSeconds(1200))));
        var service = new GridAnalyticsService(repository, Clock.fixed(now, ZoneOffset.UTC));

        var result = service.loadFor("24h");

        assertThat(result.totalConsumptionKwh()).isEqualByComparingTo("6.0");
        assertThat(result.averageConsumptionKwh()).isEqualByComparingTo("3.0");
        assertThat(result.activeMeterCount()).isEqualTo(2);
        assertThat(result.loadProfile()).singleElement()
                .extracting(point -> point.consumptionKwh())
                .isEqualTo(new BigDecimal("6.0"));
    }
}
