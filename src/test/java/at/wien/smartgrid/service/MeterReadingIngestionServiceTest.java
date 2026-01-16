package at.wien.smartgrid.service;

import at.wien.smartgrid.dto.IngestReadingRequest;
import at.wien.smartgrid.model.entity.MeterReading;
import at.wien.smartgrid.model.entity.SmartMeter;
import at.wien.smartgrid.repository.MeterReadingRepository;
import at.wien.smartgrid.repository.SmartMeterRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MeterReadingIngestionServiceTest {

    private final SmartMeterRepository meterRepository = mock(SmartMeterRepository.class);
    private final MeterReadingRepository readingRepository = mock(MeterReadingRepository.class);
    private final MeterReadingIngestionService service =
            new MeterReadingIngestionService(meterRepository, readingRepository);

    @Test
    void registersUnknownMeterAndStoresReading() {
        Instant recordedAt = Instant.parse("2026-08-20T12:00:00Z");
        var request = new IngestReadingRequest(
                "AT-001", "Vienna-Center", new BigDecimal("2.7500"), recordedAt);
        when(meterRepository.findByMeterId("AT-001")).thenReturn(Optional.empty());
        when(meterRepository.save(any(SmartMeter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(readingRepository.save(any(MeterReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.ingest(request);

        ArgumentCaptor<MeterReading> captor = ArgumentCaptor.forClass(MeterReading.class);
        verify(readingRepository).save(captor.capture());
        assertThat(captor.getValue().getRecordedAt()).isEqualTo(recordedAt);
        assertThat(response.meterId()).isEqualTo("AT-001");
        assertThat(response.consumptionKwh()).isEqualByComparingTo("2.7500");
    }
}
