package com.renault.vehiclehealth.analysis.domain.port;

public class RecallProviderUnavailableException extends RuntimeException {

    public RecallProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
