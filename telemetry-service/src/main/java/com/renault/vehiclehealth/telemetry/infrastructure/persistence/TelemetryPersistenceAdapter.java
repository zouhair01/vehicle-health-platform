package com.renault.vehiclehealth.telemetry.infrastructure.persistence;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.port.TelemetryRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TelemetryPersistenceAdapter implements TelemetryRepository {

    private final TelemetryJpaRepository jpaRepository;

    public TelemetryPersistenceAdapter(TelemetryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(Telemetry telemetry) {
        jpaRepository.save(TelemetryEntity.from(telemetry));
    }
}
