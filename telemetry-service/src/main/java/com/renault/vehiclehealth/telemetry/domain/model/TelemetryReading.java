package com.renault.vehiclehealth.telemetry.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

public record TelemetryReading(
        Vin vin,
        Instant recordedAt,
        long mileageKm,
        TirePressure tirePressure,
        double batteryVoltage,
        double engineTemperatureCelsius,
        double fuelLevelPercent,
        List<String> errorCodes) {

    private static final Pattern ERROR_CODE_FORMAT = Pattern.compile("^[PCBU][0-9A-F]{4}$");

    public TelemetryReading {
        require(vin != null, "vin is required");
        require(recordedAt != null, "recordedAt is required");
        require(tirePressure != null, "tirePressure is required");
        require(mileageKm >= 0, "mileageKm must be positive or zero");
        require(batteryVoltage >= 0 && batteryVoltage <= 30, "batteryVoltage must be between 0 and 30 V");
        require(engineTemperatureCelsius >= -60 && engineTemperatureCelsius <= 200,
                "engineTemperatureCelsius must be between -60 and 200");
        require(fuelLevelPercent >= 0 && fuelLevelPercent <= 100, "fuelLevelPercent must be between 0 and 100");

        List<String> codes = errorCodes == null ? List.of() : errorCodes;
        for (String code : codes) {
            require(code != null && ERROR_CODE_FORMAT.matcher(code).matches(), "invalid error code: " + code);
        }
        errorCodes = List.copyOf(codes);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new InvalidTelemetryException(message);
        }
    }
}
