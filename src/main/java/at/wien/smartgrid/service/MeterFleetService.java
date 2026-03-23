package at.wien.smartgrid.service;

import at.wien.smartgrid.dto.MeterSummaryResponse;
import at.wien.smartgrid.model.entity.MeterStatus;
import at.wien.smartgrid.model.entity.SmartMeter;
import at.wien.smartgrid.repository.SmartMeterRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeterFleetService {

    private final SmartMeterRepository meterRepository;

    public MeterFleetService(SmartMeterRepository meterRepository) {
        this.meterRepository = meterRepository;
    }

    @Transactional(readOnly = true)
    public List<MeterSummaryResponse> list(MeterStatus status, String gridArea) {
        String normalizedArea = gridArea == null || gridArea.isBlank() ? null : gridArea.trim();
        return meterRepository.findFleet().stream()
                .filter(meter -> status == null || meter.getStatus() == status)
                .filter(meter -> normalizedArea == null || meter.getGridArea().equalsIgnoreCase(normalizedArea))
                .map(meter -> new MeterSummaryResponse(
                        meter.getMeterId(),
                        meter.getGridArea(),
                        meter.getStatus(),
                        meter.getCreatedAt(),
                        meter.getReadingCount(),
                        meter.getLastReadingAt()))
                .toList();
    }

    @Transactional
    public MeterSummaryResponse changeStatus(String meterId, MeterStatus status) {
        SmartMeter meter = meterRepository.findByMeterId(meterId)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found: " + meterId));
        meter.changeStatus(status);
        return meterRepository.findFleet().stream()
                .filter(candidate -> candidate.getMeterId().equals(meterId))
                .findFirst()
                .map(candidate -> new MeterSummaryResponse(
                        candidate.getMeterId(),
                        candidate.getGridArea(),
                        candidate.getStatus(),
                        candidate.getCreatedAt(),
                        candidate.getReadingCount(),
                        candidate.getLastReadingAt()))
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found after status update: " + meterId));
    }
}
