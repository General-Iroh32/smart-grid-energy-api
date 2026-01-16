package at.wien.smartgrid.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record IngestReadingResponse(
        String meterId,
        BigDecimal consumptionKwh,
        Instant recordedAt,
        Instant receivedAt) {
}

