CREATE TABLE smart_meters (
    id UUID PRIMARY KEY,
    meter_id VARCHAR(64) NOT NULL UNIQUE,
    grid_area VARCHAR(80) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE meter_readings (
    id UUID PRIMARY KEY,
    smart_meter_id UUID NOT NULL REFERENCES smart_meters(id),
    consumption_kwh NUMERIC(14, 4) NOT NULL CHECK (consumption_kwh >= 0),
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_reading_meter_recorded_at UNIQUE (smart_meter_id, recorded_at)
);

CREATE INDEX idx_readings_recorded_at ON meter_readings(recorded_at);
CREATE INDEX idx_readings_meter_recorded_at ON meter_readings(smart_meter_id, recorded_at);

CREATE TABLE tariff_plans (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    price_per_kwh NUMERIC(12, 4) NOT NULL CHECK (price_per_kwh >= 0),
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL
);

