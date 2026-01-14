package at.wien.smartgrid.repository;

import at.wien.smartgrid.model.entity.SmartMeter;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmartMeterRepository extends JpaRepository<SmartMeter, UUID> {

    Optional<SmartMeter> findByMeterId(String meterId);
}

