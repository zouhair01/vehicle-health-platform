package com.renault.vehiclehealth.analysis.domain.port;

import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.Vin;

import java.util.Optional;
import java.util.UUID;

public interface HealthReportRepository {

    void save(HealthReport report);

    boolean existsByTelemetryId(UUID telemetryId);

    /**
     * @return the report of the most recently recorded telemetry for this vehicle
     */
    Optional<HealthReport> findLatestByVin(Vin vin);
}
