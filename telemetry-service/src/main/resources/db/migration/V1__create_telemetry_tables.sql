CREATE TABLE telemetry (
    id                          UUID PRIMARY KEY,
    vin                         VARCHAR(17)              NOT NULL,
    recorded_at                 TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at                 TIMESTAMP WITH TIME ZONE NOT NULL,
    mileage_km                  BIGINT                   NOT NULL,
    tire_front_left_bar         DOUBLE PRECISION         NOT NULL,
    tire_front_right_bar        DOUBLE PRECISION         NOT NULL,
    tire_rear_left_bar          DOUBLE PRECISION         NOT NULL,
    tire_rear_right_bar         DOUBLE PRECISION         NOT NULL,
    battery_voltage             DOUBLE PRECISION         NOT NULL,
    engine_temperature_celsius  DOUBLE PRECISION         NOT NULL,
    fuel_level_percent          DOUBLE PRECISION         NOT NULL
);

CREATE INDEX idx_telemetry_vin_recorded_at ON telemetry (vin, recorded_at DESC);

CREATE TABLE telemetry_error_code (
    telemetry_id UUID       NOT NULL REFERENCES telemetry (id) ON DELETE CASCADE,
    code         VARCHAR(5) NOT NULL
);

CREATE INDEX idx_telemetry_error_code_telemetry_id ON telemetry_error_code (telemetry_id);
