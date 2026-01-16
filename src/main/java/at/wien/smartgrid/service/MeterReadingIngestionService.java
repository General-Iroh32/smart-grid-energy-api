package at.wien.smartgrid.service;

import at.wien.smartgrid.dto.IngestReadingRequest;
import at.wien.smartgrid.dto.IngestReadingResponse;
import at.wien.smartgrid.model.entity.MeterReading;
import at.wien.smartgrid.model.entity.MeterStatus;
import at.wien.smartgrid.model.entity.SmartMeter;
import at.wien.smartgrid.repository.MeterReadingRepository;
import at.wien.smartgrid.repository.SmartMeterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeterReadingIngestionService {

    private final SmartMeterRepository meterRepository;
    private final MeterReadingRepository readingRepository;

    public MeterReadingIngestionService(
            SmartMeterRepository meterRepository,
            MeterReadingRepository readingRepository) {
        this.meterRepository = meterRepository;
        this.readingRepository = readingRepository;
    }

    @Transactional
    public IngestReadingResponse ingest(IngestReadingRequest request) {
        SmartMeter meter = meterRepository.findByMeterId(request.meterId())
                .orElseGet(() -> meterRepository.save(new SmartMeter(request.meterId(), request.gridArea())));

        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new IllegalStateException("Meter is not active: " + meter.getMeterId());
        }
        if (readingRepository.existsBySmartMeterIdAndRecordedAt(meter.getId(), request.recordedAt())) {
            throw new DuplicateReadingException(request.meterId());
        }

        MeterReading reading = readingRepository.save(new MeterReading(
                meter, request.consumptionKwh(), request.recordedAt()));
        return new IngestReadingResponse(
                meter.getMeterId(),
                reading.getConsumptionKwh(),
                reading.getRecordedAt(),
                reading.getReceivedAt());
    }
}

