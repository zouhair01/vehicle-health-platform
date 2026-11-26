package com.renault.vehiclehealth.analysis.infrastructure.web;

import com.renault.vehiclehealth.analysis.application.VehicleAlerts;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VehicleAlertsResponse(String vin, UUID reportId, Instant analyzedAt, int count, List<AlertResponse> alerts) {

    static VehicleAlertsResponse from(VehicleAlerts vehicleAlerts) {
        List<AlertResponse> alerts = vehicleAlerts.alerts().stream().map(AlertResponse::from).toList();
        return new VehicleAlertsResponse(
                vehicleAlerts.vin().value(), vehicleAlerts.reportId(), vehicleAlerts.analyzedAt(), alerts.size(), alerts);
    }
}
