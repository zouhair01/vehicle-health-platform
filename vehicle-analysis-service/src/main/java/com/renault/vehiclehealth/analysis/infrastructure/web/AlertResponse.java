package com.renault.vehiclehealth.analysis.infrastructure.web;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;

public record AlertResponse(AlertType type, AlertSeverity severity, String message) {

    static AlertResponse from(Alert alert) {
        return new AlertResponse(alert.type(), alert.severity(), alert.message());
    }
}
