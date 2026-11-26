package com.renault.vehiclehealth.analysis.application;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import com.renault.vehiclehealth.analysis.domain.port.HealthReportRepository;

import java.util.List;

public class GetVehicleAlertsUseCase {

    private final HealthReportRepository healthReportRepository;

    public GetVehicleAlertsUseCase(HealthReportRepository healthReportRepository) {
        this.healthReportRepository = healthReportRepository;
    }

    /**
     * @param severityFilter severity to keep, or {@code null} to return every alert
     */
    public VehicleAlerts getAlerts(Vin vin, AlertSeverity severityFilter) {
        HealthReport report = healthReportRepository.findLatestByVin(vin)
                .orElseThrow(() -> new HealthReportNotFoundException(vin));
        List<Alert> alerts = report.assessment().alerts().stream()
                .filter(alert -> severityFilter == null || alert.severity() == severityFilter)
                .toList();
        return new VehicleAlerts(vin, report.id(), report.analyzedAt(), alerts);
    }
}
