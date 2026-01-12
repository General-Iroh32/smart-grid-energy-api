package at.wien.smartgrid.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meter_readings", uniqueConstraints = @UniqueConstraint(
        name = "uk_reading_meter_recorded_at", columnNames = {"smart_meter_id", "recorded_at"}))
public class MeterReading {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "smart_meter_id", nullable = false)
    private SmartMeter smartMeter;

    @Column(name = "consumption_kwh", nullable = false, precision = 14, scale = 4)
    private BigDecimal consumptionKwh;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    protected MeterReading() {
    }

    public MeterReading(SmartMeter smartMeter, BigDecimal consumptionKwh, Instant recordedAt) {
        if (smartMeter == null || consumptionKwh == null || recordedAt == null) {
            throw new IllegalArgumentException("reading values must not be null");
        }
        if (consumptionKwh.signum() < 0) {
            throw new IllegalArgumentException("consumptionKwh must not be negative");
        }
        this.smartMeter = smartMeter;
        this.consumptionKwh = consumptionKwh;
        this.recordedAt = recordedAt;
        this.receivedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public SmartMeter getSmartMeter() {
        return smartMeter;
    }

    public BigDecimal getConsumptionKwh() {
        return consumptionKwh;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}

