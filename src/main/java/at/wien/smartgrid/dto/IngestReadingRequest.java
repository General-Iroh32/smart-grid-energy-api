package at.wien.smartgrid.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record IngestReadingRequest(
        @NotBlank @Size(min = 3, max = 64) @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]+$") String meterId,
        @NotBlank @Size(min = 2, max = 80) String gridArea,
        @NotNull @DecimalMin("0.0") @DecimalMax("1000000.0000") @Digits(integer = 10, fraction = 4)
                BigDecimal consumptionKwh,
        @NotNull @PastOrPresent Instant recordedAt) {
}
