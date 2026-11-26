package com.renault.vehiclehealth.analysis.application;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.Vin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VehicleAlerts(Vin vin, UUID reportId, Instant analyzedAt, List<Alert> alerts) {

    public VehicleAlerts {
        alerts = List.copyOf(alerts);
    }
}
