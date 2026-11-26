package com.renault.vehiclehealth.analysis.domain.port;

import com.renault.vehiclehealth.analysis.domain.model.HealthReport;

import java.util.UUID;

public interface HealthReportRepository {

    void save(HealthReport report);

    boolean existsByTelemetryId(UUID telemetryId);
}
