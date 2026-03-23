package at.wien.smartgrid.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.wien.smartgrid.dto.MeterSummaryResponse;
import at.wien.smartgrid.model.entity.MeterStatus;
import at.wien.smartgrid.service.MeterFleetService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MeterFleetController.class)
class MeterFleetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeterFleetService meterFleetService;

    @Test
    void listsMetersUsingOperationalFilters() throws Exception {
        when(meterFleetService.list(MeterStatus.ACTIVE, "Vienna-Center"))
                .thenReturn(List.of(summary(MeterStatus.ACTIVE)));

        mockMvc.perform(get("/api/v1/meters")
                        .param("status", "ACTIVE")
                        .param("gridArea", "Vienna-Center"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].meterId").value("AT-VIE-1001"))
                .andExpect(jsonPath("$[0].readingCount").value(48));
    }

    @Test
    void changesMeterStatus() throws Exception {
        when(meterFleetService.changeStatus(eq("AT-VIE-1001"), eq(MeterStatus.INACTIVE)))
                .thenReturn(summary(MeterStatus.INACTIVE));

        mockMvc.perform(patch("/api/v1/meters/AT-VIE-1001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"INACTIVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void allowsDashboardPreflightForStatusChanges() throws Exception {
        mockMvc.perform(options("/api/v1/meters/AT-VIE-1001/status")
                        .header("Origin", "http://127.0.0.1:4200")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("PATCH")));
    }

    private MeterSummaryResponse summary(MeterStatus status) {
        Instant now = Instant.parse("2026-08-21T08:00:00Z");
        return new MeterSummaryResponse("AT-VIE-1001", "Vienna-Center", status, now, 48, now);
    }
}
