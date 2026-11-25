package com.renault.vehiclehealth.telemetry.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface TelemetryJpaRepository extends JpaRepository<TelemetryEntity, UUID> {
}
