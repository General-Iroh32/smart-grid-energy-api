package at.wien.smartgrid.service;

import at.wien.smartgrid.dto.AggregatedConsumptionResponse;
import at.wien.smartgrid.dto.GridLoadPoint;
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
        AnalyticsTimespan timespan = AnalyticsTimespan.parse(rawTimespan);
        Instant to = clock.instant();
        Instant from = to.minus(timespan.duration());
        List<MeterReading> readings =
                readingRepository.findAllByRecordedAtGreaterThanEqualAndRecordedAtLessThan(from, to);

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

    private Instant bucketStart(Instant timestamp, AnalyticsTimespan timespan) {
        if (timespan == AnalyticsTimespan.SEVEN_DAYS) {
            int hour = timestamp.atZone(ZoneOffset.UTC).getHour();
            return timestamp.truncatedTo(ChronoUnit.DAYS).plus(hour / 6 * 6L, ChronoUnit.HOURS);
        }
        return timestamp.truncatedTo(ChronoUnit.HOURS);
    }
}

