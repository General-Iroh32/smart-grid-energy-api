package at.wien.smartgrid.controller;

import at.wien.smartgrid.service.DuplicateReadingException;
import at.wien.smartgrid.service.MeterReadingIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeterReadingController.class)
class MeterReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeterReadingIngestionService ingestionService;

    @Test
    void returnsStructuredValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/readings/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "meterId": "",
                                  "gridArea": "Vienna-Center",
                                  "consumptionKwh": -1,
                                  "recordedAt": "2026-08-20T12:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.violations.meterId").exists())
                .andExpect(jsonPath("$.violations.consumptionKwh").exists());
    }

    @Test
    void mapsDuplicateReadingsToConflict() throws Exception {
        when(ingestionService.ingest(any()))
                .thenThrow(new DuplicateReadingException("AT-001"));

        mockMvc.perform(post("/api/v1/readings/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "meterId": "AT-001",
                                  "gridArea": "Vienna-Center",
                                  "consumptionKwh": 2.5,
                                  "recordedAt": "2026-08-20T12:00:00Z"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate meter reading"));
    }

    @Test
    void allowsTheLoopbackDashboardOrigin() throws Exception {
        mockMvc.perform(post("/api/v1/readings/ingest")
                        .header("Origin", "http://127.0.0.1:4200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin", "http://127.0.0.1:4200"));
    }
}
