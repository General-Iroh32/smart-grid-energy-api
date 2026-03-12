package at.wien.smartgrid.service;

import at.wien.smartgrid.dto.AggregatedConsumptionResponse;
import at.wien.smartgrid.dto.GridAreaAnalyticsResponse;
import at.wien.smartgrid.dto.GridAreaLoadResponse;
import at.wien.smartgrid.dto.GridLoadPoint;
import at.wien.smartgrid.dto.LoadAnomalyResponse;
import at.wien.smartgrid.model.entity.MeterReading;
import at.wien.smartgrid.repository.MeterReadingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GridAnalyticsService {

    private static final int RESULT_SCALE = 4;
    private final MeterReadingRepository readingRepository;
    private final Clock clock;

    public GridAnalyticsService(MeterReadingRepository readingRepository, Clock clock) {
        this.readingRepository = readingRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AggregatedConsumptionResponse loadFor(String rawTimespan) {
        AnalyticsWindow window = readingsFor(rawTimespan);
        AnalyticsTimespan timespan = window.timespan();
        Instant to = window.to();
        Instant from = window.from();
        List<MeterReading> readings = window.readings();

        BigDecimal total = readings.stream()
                .map(MeterReading::getConsumptionKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = readings.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(readings.size()), RESULT_SCALE, RoundingMode.HALF_UP);
        BigDecimal peak = readings.stream()
                .map(MeterReading::getConsumptionKwh)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        long activeMeters = readings.stream()
                .map(reading -> reading.getSmartMeter().getMeterId())
                .distinct()
                .count();

        Map<Instant, BigDecimal> buckets = readings.stream().collect(Collectors.groupingBy(
                reading -> bucketStart(reading.getRecordedAt(), timespan),
                LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO, MeterReading::getConsumptionKwh, BigDecimal::add)));
        List<GridLoadPoint> profile = buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new GridLoadPoint(entry.getKey(), entry.getValue()))
                .toList();

        return new AggregatedConsumptionResponse(
                timespan.queryValue(), from, to, total, average, peak, readings.size(), activeMeters, profile);
    }

    @Transactional(readOnly = true)
    public GridAreaAnalyticsResponse loadByArea(String rawTimespan) {
        AnalyticsWindow window = readingsFor(rawTimespan);
        BigDecimal grandTotal = window.readings().stream()
                .map(MeterReading::getConsumptionKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<MeterReading>> byArea = window.readings().stream()
                .collect(Collectors.groupingBy(
                        reading -> reading.getSmartMeter().getGridArea(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<GridAreaLoadResponse> areas = byArea.entrySet().stream()
                .map(entry -> areaSummary(entry.getKey(), entry.getValue(), grandTotal))
                .sorted(Comparator.comparing(GridAreaLoadResponse::totalConsumptionKwh).reversed())
                .toList();

        return new GridAreaAnalyticsResponse(
                window.timespan().queryValue(), window.from(), window.to(), grandTotal, areas);
    }

    @Transactional(readOnly = true)
    public List<LoadAnomalyResponse> anomalies(String rawTimespan, BigDecimal thresholdKwh) {
        if (thresholdKwh == null || thresholdKwh.signum() <= 0) {
            throw new IllegalArgumentException("thresholdKwh must be greater than zero");
        }
        AnalyticsWindow window = readingsFor(rawTimespan);
        return window.readings().stream()
                .filter(reading -> reading.getConsumptionKwh().compareTo(thresholdKwh) > 0)
                .sorted(Comparator.comparing(MeterReading::getConsumptionKwh).reversed())
                .limit(50)
                .map(reading -> new LoadAnomalyResponse(
                        reading.getSmartMeter().getMeterId(),
                        reading.getSmartMeter().getGridArea(),
                        reading.getRecordedAt(),
                        reading.getConsumptionKwh(),
                        thresholdKwh,
                        reading.getConsumptionKwh()
                                .subtract(thresholdKwh)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(thresholdKwh, 2, RoundingMode.HALF_UP)))
                .toList();
    }

    private GridAreaLoadResponse areaSummary(
            String gridArea, List<MeterReading> readings, BigDecimal grandTotal) {
        BigDecimal total = readings.stream()
                .map(MeterReading::getConsumptionKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = total.divide(BigDecimal.valueOf(readings.size()), RESULT_SCALE, RoundingMode.HALF_UP);
        BigDecimal peak = readings.stream()
                .map(MeterReading::getConsumptionKwh)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        BigDecimal share = grandTotal.signum() == 0
                ? BigDecimal.ZERO
                : total.multiply(BigDecimal.valueOf(100)).divide(grandTotal, 2, RoundingMode.HALF_UP);
        BigDecimal peakRatio = average.signum() == 0
                ? BigDecimal.ZERO
                : peak.divide(average, 2, RoundingMode.HALF_UP);
        String state = peakRatio.compareTo(new BigDecimal("2.00")) >= 0
                ? "CRITICAL"
                : peakRatio.compareTo(new BigDecimal("1.50")) >= 0 ? "ELEVATED" : "NORMAL";
        long meters = readings.stream()
                .map(reading -> reading.getSmartMeter().getMeterId())
                .distinct()
                .count();
        return new GridAreaLoadResponse(
                gridArea, total, average, peak, readings.size(), meters, share, state);
    }

    private AnalyticsWindow readingsFor(String rawTimespan) {
        AnalyticsTimespan timespan = AnalyticsTimespan.parse(rawTimespan);
        Instant to = clock.instant();
        Instant from = to.minus(timespan.duration());
        List<MeterReading> readings =
                readingRepository.findAllByRecordedAtGreaterThanEqualAndRecordedAtLessThan(from, to);
        return new AnalyticsWindow(timespan, from, to, readings);
    }

    private Instant bucketStart(Instant timestamp, AnalyticsTimespan timespan) {
        if (timespan == AnalyticsTimespan.SEVEN_DAYS) {
            int hour = timestamp.atZone(ZoneOffset.UTC).getHour();
            return timestamp.truncatedTo(ChronoUnit.DAYS).plus(hour / 6 * 6L, ChronoUnit.HOURS);
        }
        return timestamp.truncatedTo(ChronoUnit.HOURS);
    }

    private record AnalyticsWindow(
            AnalyticsTimespan timespan,
            Instant from,
            Instant to,
            List<MeterReading> readings) {
    }
}
