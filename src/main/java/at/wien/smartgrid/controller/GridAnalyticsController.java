package at.wien.smartgrid.controller;

import at.wien.smartgrid.dto.AggregatedConsumptionResponse;
import at.wien.smartgrid.service.GridAnalyticsService;
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
    public AggregatedConsumptionResponse gridLoad(
            @RequestParam(defaultValue = "24h") String timespan) {
        return analyticsService.loadFor(timespan);
    }
}

