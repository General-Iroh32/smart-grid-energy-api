package at.wien.smartgrid.dto;

import java.math.BigDecimal;

public record GridAreaLoadResponse(
        String gridArea,
        BigDecimal totalConsumptionKwh,
        BigDecimal averageConsumptionKwh,
        BigDecimal peakConsumptionKwh,
        long readingCount,
        long activeMeterCount,
        BigDecimal loadSharePercent,
        String operationalState) {
}
