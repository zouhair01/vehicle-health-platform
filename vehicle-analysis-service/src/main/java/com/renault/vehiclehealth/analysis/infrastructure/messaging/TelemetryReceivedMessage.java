package com.renault.vehiclehealth.analysis.infrastructure.messaging;

import com.renault.vehiclehealth.analysis.domain.model.TirePressure;
import com.renault.vehiclehealth.analysis.domain.model.VehicleTelemetry;
import com.renault.vehiclehealth.analysis.domain.model.Vin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TelemetryReceivedMessage(
        String eventType,
        Instant occurredAt,
        UUID telemetryId,
        String vin,
        Instant recordedAt,
        Long mileageKm,
        TirePressurePayload tirePressure,
        Double batteryVoltage,
        Double engineTemperatureCelsius,
        Double fuelLevelPercent,
        List<String> errorCodes) {

    public record TirePressurePayload(
            double frontLeftBar, double frontRightBar, double rearLeftBar, double rearRightBar) {
    }

    VehicleTelemetry toDomain() {
        return new VehicleTelemetry(
                telemetryId,
                new Vin(vin),
                recordedAt,
                mileageKm,
                new TirePressure(
                        tirePressure.frontLeftBar(),
                        tirePressure.frontRightBar(),
                        tirePressure.rearLeftBar(),
                        tirePressure.rearRightBar()),
                batteryVoltage,
                engineTemperatureCelsius,
                fuelLevelPercent,
                errorCodes);
    }
}
