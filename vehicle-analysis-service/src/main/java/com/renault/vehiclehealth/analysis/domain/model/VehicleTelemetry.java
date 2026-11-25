package com.renault.vehiclehealth.analysis.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record VehicleTelemetry(
        UUID telemetryId,
        Vin vin,
        Instant recordedAt,
        long mileageKm,
        TirePressure tirePressure,
        double batteryVoltage,
        double engineTemperatureCelsius,
        double fuelLevelPercent,
        List<String> errorCodes) {

    public VehicleTelemetry {
        Objects.requireNonNull(telemetryId, "telemetryId is required");
        Objects.requireNonNull(vin, "vin is required");
        Objects.requireNonNull(recordedAt, "recordedAt is required");
        Objects.requireNonNull(tirePressure, "tirePressure is required");
        errorCodes = errorCodes == null ? List.of() : List.copyOf(errorCodes);
    }
}
