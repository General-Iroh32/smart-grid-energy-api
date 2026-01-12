package at.wien.smartgrid.model.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeterReadingTest {

    @Test
    void rejectsNegativeConsumption() {
        SmartMeter meter = new SmartMeter("AT-001", "Vienna-Center");

        assertThatThrownBy(() -> new MeterReading(meter, new BigDecimal("-0.01"), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be negative");
    }
}
