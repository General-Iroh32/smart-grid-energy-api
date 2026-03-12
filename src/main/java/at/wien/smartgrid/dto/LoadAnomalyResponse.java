package at.wien.smartgrid.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LoadAnomalyResponse(
        String meterId,
        String gridArea,
        Instant recordedAt,
        BigDecimal consumptionKwh,
        BigDecimal thresholdKwh,
        BigDecimal excessPercent) {
}
