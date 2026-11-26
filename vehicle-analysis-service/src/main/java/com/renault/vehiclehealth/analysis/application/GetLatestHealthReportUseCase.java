package com.renault.vehiclehealth.analysis.application;

import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import com.renault.vehiclehealth.analysis.domain.port.HealthReportRepository;

public class GetLatestHealthReportUseCase {

    private final HealthReportRepository healthReportRepository;

    public GetLatestHealthReportUseCase(HealthReportRepository healthReportRepository) {
        this.healthReportRepository = healthReportRepository;
    }

    public HealthReport getLatest(Vin vin) {
        return healthReportRepository.findLatestByVin(vin)
                .orElseThrow(() -> new HealthReportNotFoundException(vin));
    }
}
