package com.renault.vehiclehealth.telemetry.domain.model;

public class InvalidTelemetryException extends RuntimeException {

    public InvalidTelemetryException(String message) {
        super(message);
    }
}
