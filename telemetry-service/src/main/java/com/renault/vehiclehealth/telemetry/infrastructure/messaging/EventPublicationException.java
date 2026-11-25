package com.renault.vehiclehealth.telemetry.infrastructure.messaging;

public class EventPublicationException extends RuntimeException {

    public EventPublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
