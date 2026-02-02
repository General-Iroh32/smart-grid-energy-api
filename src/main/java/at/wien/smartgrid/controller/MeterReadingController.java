package at.wien.smartgrid.controller;

import at.wien.smartgrid.dto.IngestReadingRequest;
import at.wien.smartgrid.dto.IngestReadingResponse;
import at.wien.smartgrid.service.MeterReadingIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/readings")
public class MeterReadingController {

    private final MeterReadingIngestionService ingestionService;

    public MeterReadingController(MeterReadingIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingest one smart-meter reading")
    @ApiResponse(responseCode = "201", description = "Reading accepted and persisted")
    @ApiResponse(responseCode = "400", description = "Payload validation failed")
    @ApiResponse(responseCode = "409", description = "Reading already exists")
    public ResponseEntity<IngestReadingResponse> ingest(@Valid @RequestBody IngestReadingRequest request) {
        IngestReadingResponse response = ingestionService.ingest(request);
        URI location = URI.create("/api/v1/meters/" + response.meterId() + "/readings");
        return ResponseEntity.created(location).body(response);
    }
}
