package at.wien.smartgrid.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tariff_plans")
public class TariffPlan {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(name = "price_per_kwh", nullable = false, precision = 12, scale = 4)
    private BigDecimal pricePerKwh;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    protected TariffPlan() {
    }

    public TariffPlan(String name, BigDecimal pricePerKwh, Instant validFrom) {
        if (name == null || name.isBlank() || pricePerKwh == null || validFrom == null) {
            throw new IllegalArgumentException("tariff values must not be blank or null");
        }
        if (pricePerKwh.signum() < 0) {
            throw new IllegalArgumentException("pricePerKwh must not be negative");
        }
        this.name = name.trim();
        this.pricePerKwh = pricePerKwh;
        this.validFrom = validFrom;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPricePerKwh() {
        return pricePerKwh;
    }

    public Instant getValidFrom() {
        return validFrom;
    }
}

