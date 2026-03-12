package at.wien.smartgrid.controller;

import at.wien.smartgrid.dto.AggregatedConsumptionResponse;
import at.wien.smartgrid.dto.GridAreaAnalyticsResponse;
import at.wien.smartgrid.dto.LoadAnomalyResponse;
import at.wien.smartgrid.service.GridAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class GridAnalyticsController {

    private final GridAnalyticsService analyticsService;

    public GridAnalyticsController(GridAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/grid-load")
    @Operation(summary = "Aggregate grid load for a supported timespan")
    public AggregatedConsumptionResponse gridLoad(
            @RequestParam(defaultValue = "24h") String timespan) {
        return analyticsService.loadFor(timespan);
    }

    @GetMapping("/grid-areas")
    @Operation(summary = "Compare load and operational state across grid areas")
    public GridAreaAnalyticsResponse gridAreas(
            @RequestParam(defaultValue = "24h") String timespan) {
        return analyticsService.loadByArea(timespan);
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Find readings above a configurable load threshold")
    public List<LoadAnomalyResponse> anomalies(
            @RequestParam(defaultValue = "24h") String timespan,
            @RequestParam(defaultValue = "4.5") BigDecimal thresholdKwh) {
        return analyticsService.anomalies(timespan, thresholdKwh);
    }
}
