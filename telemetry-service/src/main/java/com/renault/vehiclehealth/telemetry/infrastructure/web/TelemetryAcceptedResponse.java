package com.renault.vehiclehealth.telemetry.infrastructure.web;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;

import java.time.Instant;
import java.util.UUID;

public record TelemetryAcceptedResponse(UUID telemetryId, String vin, Instant receivedAt) {

    static TelemetryAcceptedResponse from(Telemetry telemetry) {
        return new TelemetryAcceptedResponse(
                telemetry.id(), telemetry.reading().vin().value(), telemetry.receivedAt());
    }
}
