package com.renault.vehiclehealth.analysis.domain.model;

import java.util.Objects;

public record Alert(AlertType type, AlertSeverity severity, String message) {

    public Alert {
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(severity, "severity is required");
        Objects.requireNonNull(message, "message is required");
    }
}
