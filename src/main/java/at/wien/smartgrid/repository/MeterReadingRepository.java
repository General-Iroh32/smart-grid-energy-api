package at.wien.smartgrid.repository;

import at.wien.smartgrid.model.entity.MeterReading;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {

    boolean existsBySmartMeterIdAndRecordedAt(UUID smartMeterId, Instant recordedAt);

    @EntityGraph(attributePaths = "smartMeter")
    List<MeterReading> findAllByRecordedAtGreaterThanEqualAndRecordedAtLessThan(
            Instant fromInclusive, Instant toExclusive);
}

