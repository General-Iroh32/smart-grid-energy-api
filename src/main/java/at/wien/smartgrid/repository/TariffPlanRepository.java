package at.wien.smartgrid.repository;

import at.wien.smartgrid.model.entity.TariffPlan;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffPlanRepository extends JpaRepository<TariffPlan, UUID> {

    Optional<TariffPlan> findFirstByValidFromLessThanEqualOrderByValidFromDesc(Instant at);
}

