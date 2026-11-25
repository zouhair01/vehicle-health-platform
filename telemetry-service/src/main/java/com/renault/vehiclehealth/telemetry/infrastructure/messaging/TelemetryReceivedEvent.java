package com.renault.vehiclehealth.telemetry.infrastructure.messaging;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.model.TelemetryReading;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TelemetryReceivedEvent(
        String eventType,
        Instant occurredAt,
        UUID telemetryId,
        String vin,
        Instant recordedAt,
        long mileageKm,
        TirePressurePayload tirePressure,
        double batteryVoltage,
        double engineTemperatureCelsius,
        double fuelLevelPercent,
        List<String> errorCodes) {

    static final String EVENT_TYPE = "TelemetryReceived";

    public record TirePressurePayload(
            double frontLeftBar, double frontRightBar, double rearLeftBar, double rearRightBar) {
    }

    static TelemetryReceivedEvent from(Telemetry telemetry) {
        TelemetryReading reading = telemetry.reading();
        return new TelemetryReceivedEvent(
                EVENT_TYPE,
                telemetry.receivedAt(),
                telemetry.id(),
                reading.vin().value(),
                reading.recordedAt(),
                reading.mileageKm(),
                new TirePressurePayload(
                        reading.tirePressure().frontLeftBar(),
                        reading.tirePressure().frontRightBar(),
                        reading.tirePressure().rearLeftBar(),
                        reading.tirePressure().rearRightBar()),
                reading.batteryVoltage(),
                reading.engineTemperatureCelsius(),
                reading.fuelLevelPercent(),
                reading.errorCodes());
    }
}
