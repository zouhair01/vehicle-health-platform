package com.renault.vehiclehealth.analysis.infrastructure.web;

import com.renault.vehiclehealth.analysis.application.GetLatestHealthReportUseCase;
import com.renault.vehiclehealth.analysis.application.GetVehicleAlertsUseCase;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles/{vin}")
public class VehicleHealthController {

    private final GetLatestHealthReportUseCase getLatestHealthReport;
    private final GetVehicleAlertsUseCase getVehicleAlerts;

    public VehicleHealthController(GetLatestHealthReportUseCase getLatestHealthReport,
                                   GetVehicleAlertsUseCase getVehicleAlerts) {
        this.getLatestHealthReport = getLatestHealthReport;
        this.getVehicleAlerts = getVehicleAlerts;
    }

    @GetMapping("/health-report")
    public HealthReportResponse getHealthReport(@PathVariable String vin) {
        return HealthReportResponse.from(getLatestHealthReport.getLatest(new Vin(vin)));
    }

    @GetMapping("/alerts")
    public VehicleAlertsResponse getAlerts(@PathVariable String vin,
                                           @RequestParam(required = false) AlertSeverity severity) {
        return VehicleAlertsResponse.from(getVehicleAlerts.getAlerts(new Vin(vin), severity));
    }
}
