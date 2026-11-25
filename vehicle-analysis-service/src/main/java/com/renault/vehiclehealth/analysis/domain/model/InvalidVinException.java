package com.renault.vehiclehealth.analysis.domain.model;

public class InvalidVinException extends RuntimeException {

    public InvalidVinException(String message) {
        super(message);
    }
}
