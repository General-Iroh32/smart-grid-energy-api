package at.wien.smartgrid.controller;

import at.wien.smartgrid.dto.IngestReadingRequest;
import at.wien.smartgrid.dto.IngestReadingResponse;
import at.wien.smartgrid.service.MeterReadingIngestionService;
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
    public ResponseEntity<IngestReadingResponse> ingest(@Valid @RequestBody IngestReadingRequest request) {
        IngestReadingResponse response = ingestionService.ingest(request);
        URI location = URI.create("/api/v1/meters/" + response.meterId() + "/readings");
        return ResponseEntity.created(location).body(response);
    }
}

