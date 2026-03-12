package at.wien.smartgrid.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record GridAreaAnalyticsResponse(
        String timespan,
        Instant from,
        Instant to,
        BigDecimal totalConsumptionKwh,
        List<GridAreaLoadResponse> areas) {
}
