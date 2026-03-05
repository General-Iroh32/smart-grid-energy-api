package at.wien.smartgrid.controller;

import at.wien.smartgrid.dto.MeterSummaryResponse;
import at.wien.smartgrid.dto.UpdateMeterStatusRequest;
import at.wien.smartgrid.model.entity.MeterStatus;
import at.wien.smartgrid.service.MeterFleetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meters")
public class MeterFleetController {

    private final MeterFleetService meterFleetService;

    public MeterFleetController(MeterFleetService meterFleetService) {
        this.meterFleetService = meterFleetService;
    }

    @GetMapping
    @Operation(summary = "List the smart-meter fleet with optional filters")
    public List<MeterSummaryResponse> list(
            @RequestParam(required = false) MeterStatus status,
            @RequestParam(required = false) String gridArea) {
        return meterFleetService.list(status, gridArea);
    }

    @PatchMapping("/{meterId}/status")
    @Operation(summary = "Activate or deactivate a smart meter")
    public MeterSummaryResponse changeStatus(
            @PathVariable String meterId,
            @Valid @RequestBody UpdateMeterStatusRequest request) {
        return meterFleetService.changeStatus(meterId, request.status());
    }
}
