package com.renault.vehiclehealth.analysis.infrastructure.messaging;

public class InvalidTelemetryMessageException extends RuntimeException {

    public InvalidTelemetryMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
