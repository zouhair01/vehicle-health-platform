package com.renault.vehiclehealth.analysis.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record HealthReport(
        UUID id,
        Vin vin,
        UUID telemetryId,
        Instant telemetryRecordedAt,
        Instant analyzedAt,
        HealthAssessment assessment) {

    public HealthReport {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(vin, "vin is required");
        Objects.requireNonNull(telemetryId, "telemetryId is required");
        Objects.requireNonNull(telemetryRecordedAt, "telemetryRecordedAt is required");
        Objects.requireNonNull(analyzedAt, "analyzedAt is required");
        Objects.requireNonNull(assessment, "assessment is required");
    }
}
