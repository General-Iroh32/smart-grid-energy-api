package at.wien.smartgrid.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AggregatedConsumptionResponse(
        String timespan,
        Instant from,
        Instant to,
        BigDecimal totalConsumptionKwh,
        BigDecimal averageConsumptionKwh,
        BigDecimal peakConsumptionKwh,
        long readingCount,
        long activeMeterCount,
        List<GridLoadPoint> loadProfile) {
}

