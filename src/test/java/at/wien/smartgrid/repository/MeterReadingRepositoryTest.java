package at.wien.smartgrid.repository;

import at.wien.smartgrid.model.entity.MeterReading;
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
}
