package at.wien.smartgrid.repository;

import at.wien.smartgrid.model.entity.SmartMeter;
import at.wien.smartgrid.model.entity.MeterStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmartMeterRepository extends JpaRepository<SmartMeter, UUID> {

    Optional<SmartMeter> findByMeterId(String meterId);

    @Query("""
            select m.meterId as meterId,
                   m.gridArea as gridArea,
                   m.status as status,
                   m.createdAt as createdAt,
                   count(r.id) as readingCount,
                   max(r.recordedAt) as lastReadingAt
            from SmartMeter m
            left join MeterReading r on r.smartMeter = m
            group by m.id, m.meterId, m.gridArea, m.status, m.createdAt
            order by m.meterId
            """)
    List<MeterFleetView> findFleet();

    interface MeterFleetView {
        String getMeterId();

        String getGridArea();

        MeterStatus getStatus();

        Instant getCreatedAt();

        long getReadingCount();

        Instant getLastReadingAt();
    }
}
