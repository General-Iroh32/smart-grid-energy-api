package at.wien.smartgrid.dto;

import at.wien.smartgrid.model.entity.MeterStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMeterStatusRequest(@NotNull MeterStatus status) {
}
