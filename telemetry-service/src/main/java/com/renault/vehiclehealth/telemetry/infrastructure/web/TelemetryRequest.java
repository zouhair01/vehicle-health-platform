package com.renault.vehiclehealth.telemetry.infrastructure.web;

import com.renault.vehiclehealth.telemetry.domain.model.TelemetryReading;
import com.renault.vehiclehealth.telemetry.domain.model.TirePressure;
import com.renault.vehiclehealth.telemetry.domain.model.Vin;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;
import java.util.List;

public record TelemetryRequest(
        @NotBlank String vin,
        @NotNull @PastOrPresent Instant recordedAt,
        @NotNull Long mileageKm,
        @NotNull @Valid TirePressureRequest tirePressure,
        @NotNull Double batteryVoltage,
        @NotNull Double engineTemperatureCelsius,
        @NotNull Double fuelLevelPercent,
        List<String> errorCodes) {

    public record TirePressureRequest(
            @NotNull Double frontLeftBar,
            @NotNull Double frontRightBar,
            @NotNull Double rearLeftBar,
            @NotNull Double rearRightBar) {
    }

    TelemetryReading toReading() {
        return new TelemetryReading(
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
