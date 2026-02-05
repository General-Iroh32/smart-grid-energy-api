package at.wien.smartgrid.config;

import at.wien.smartgrid.model.entity.MeterReading;
import at.wien.smartgrid.model.entity.SmartMeter;
import at.wien.smartgrid.repository.MeterReadingRepository;
import at.wien.smartgrid.repository.SmartMeterRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {

    private final SmartMeterRepository meterRepository;
    private final MeterReadingRepository readingRepository;
    private final Clock clock;

    public DemoDataInitializer(
            SmartMeterRepository meterRepository,
            MeterReadingRepository readingRepository,
            Clock clock) {
        this.meterRepository = meterRepository;
        this.readingRepository = readingRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (readingRepository.count() > 0) {
            return;
        }
        SmartMeter center = meterRepository.save(new SmartMeter("AT-DEMO-1001", "Vienna-Center"));
        SmartMeter west = meterRepository.save(new SmartMeter("AT-DEMO-2001", "Vienna-West"));
        Instant currentHour = clock.instant().minusSeconds(300);
        for (int hour = 23; hour >= 0; hour--) {
            BigDecimal baseline = new BigDecimal("2.40").add(BigDecimal.valueOf((23 - hour) % 6).movePointLeft(1));
            readingRepository.save(new MeterReading(center, baseline, currentHour.minusSeconds(hour * 3600L)));
            readingRepository.save(new MeterReading(
                    west, baseline.multiply(new BigDecimal("0.72")), currentHour.minusSeconds(hour * 3600L + 120)));
        }
    }
}
