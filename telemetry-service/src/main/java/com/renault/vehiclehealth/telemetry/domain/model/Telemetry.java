package com.renault.vehiclehealth.telemetry.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Telemetry(UUID id, TelemetryReading reading, Instant receivedAt) {

    public Telemetry {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(reading, "reading is required");
        Objects.requireNonNull(receivedAt, "receivedAt is required");
    }
}
