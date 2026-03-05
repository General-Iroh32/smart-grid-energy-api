package at.wien.smartgrid.dto;

import at.wien.smartgrid.model.entity.MeterStatus;
import java.time.Instant;

public record MeterSummaryResponse(
        String meterId,
        String gridArea,
        MeterStatus status,
        Instant createdAt,
        long readingCount,
        Instant lastReadingAt) {
}
