package at.wien.smartgrid.repository;

import at.wien.smartgrid.model.entity.MeterReading;
import at.wien.smartgrid.model.entity.MeterStatus;
import at.wien.smartgrid.model.entity.SmartMeter;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MeterReadingRepositoryTest {

    @Autowired
    private SmartMeterRepository smartMeterRepository;

    @Autowired
    private MeterReadingRepository readingRepository;

    @Test
    void findsReadingsInsideRequestedWindow() {
        Instant noon = Instant.parse("2026-08-20T12:00:00Z");
        SmartMeter meter = smartMeterRepository.save(new SmartMeter("AT-001", "Vienna-Center"));
        readingRepository.save(new MeterReading(meter, new BigDecimal("3.2500"), noon));

        var readings = readingRepository.findAllByRecordedAtGreaterThanEqualAndRecordedAtLessThan(
                noon.minusSeconds(60), noon.plusSeconds(60));

        assertThat(readings)
                .singleElement()
                .extracting(MeterReading::getConsumptionKwh)
                .isEqualTo(new BigDecimal("3.2500"));
    }

    @Test
    void projectsFleetTelemetryWithoutLoadingReadingCollections() {
        Instant noon = Instant.parse("2026-08-20T12:00:00Z");
        SmartMeter center = smartMeterRepository.save(new SmartMeter("AT-101", "Vienna-Center"));
        SmartMeter west = smartMeterRepository.save(new SmartMeter("AT-202", "Vienna-West"));
        readingRepository.save(new MeterReading(center, new BigDecimal("2.5000"), noon));
        readingRepository.save(new MeterReading(center, new BigDecimal("3.0000"), noon.plusSeconds(60)));
        readingRepository.save(new MeterReading(west, new BigDecimal("1.5000"), noon));

        var fleet = smartMeterRepository.findFleet().stream()
                .filter(meter -> meter.getStatus() == MeterStatus.ACTIVE)
                .filter(meter -> meter.getGridArea().equals("Vienna-Center"))
                .toList();

        assertThat(fleet).singleElement().satisfies(meter -> {
            assertThat(meter.getMeterId()).isEqualTo("AT-101");
            assertThat(meter.getReadingCount()).isEqualTo(2);
            assertThat(meter.getLastReadingAt()).isEqualTo(noon.plusSeconds(60));
        });
    }
}
