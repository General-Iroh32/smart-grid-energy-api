package at.wien.smartgrid.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record IngestReadingRequest(
        @NotBlank @Size(max = 64) String meterId,
        @NotBlank @Size(max = 80) String gridArea,
        @NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 4) BigDecimal consumptionKwh,
        @NotNull @PastOrPresent Instant recordedAt) {
}

