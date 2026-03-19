package at.wien.smartgrid;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SmartGridApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ingestedReadingAppearsInGridAnalytics() throws Exception {
        String recordedAt = Instant.now().minusSeconds(60).toString();
        mockMvc.perform(post("/api/v1/readings/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "meterId": "AT-INTEGRATION-001",
                                  "gridArea": "Vienna-Center",
                                  "consumptionKwh": 7.25,
                                  "recordedAt": "%s"
                                }
                                """.formatted(recordedAt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.meterId").value("AT-INTEGRATION-001"));

        mockMvc.perform(get("/api/v1/analytics/grid-load").param("timespan", "1h"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readingCount").value(1))
                .andExpect(jsonPath("$.totalConsumptionKwh").value(7.25));
    }

    @Test
    void servesTheVersionedOpenApiContract() throws Exception {
        mockMvc.perform(get("/openapi/smart-grid-api.yaml"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("openapi: 3.1.0")))
                .andExpect(content().string(containsString("operationId: listMeters")));
    }
}
