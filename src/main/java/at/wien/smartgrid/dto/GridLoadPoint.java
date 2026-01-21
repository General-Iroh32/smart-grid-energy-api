package at.wien.smartgrid.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GridLoadPoint(Instant timestamp, BigDecimal consumptionKwh) {
}

