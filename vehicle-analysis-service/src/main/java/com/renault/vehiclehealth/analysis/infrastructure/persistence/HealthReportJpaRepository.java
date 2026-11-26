package com.renault.vehiclehealth.analysis.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface HealthReportJpaRepository extends JpaRepository<HealthReportEntity, UUID> {

    boolean existsByTelemetryId(UUID telemetryId);
}
