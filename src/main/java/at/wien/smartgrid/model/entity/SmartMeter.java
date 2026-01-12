package at.wien.smartgrid.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "smart_meters")
public class SmartMeter {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "meter_id", nullable = false, unique = true, updatable = false, length = 64)
    private String meterId;

    @Column(name = "grid_area", nullable = false, length = 80)
    private String gridArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MeterStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SmartMeter() {
    }

    public SmartMeter(String meterId, String gridArea) {
        this.meterId = requireText(meterId, "meterId");
        this.gridArea = requireText(gridArea, "gridArea");
        this.status = MeterStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getMeterId() {
        return meterId;
    }

    public String getGridArea() {
        return gridArea;
    }

    public MeterStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof SmartMeter meter && Objects.equals(meterId, meter.meterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meterId);
    }
}

