package com.renault.vehiclehealth.analysis.infrastructure.persistence;

import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import com.renault.vehiclehealth.analysis.domain.port.HealthReportRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class HealthReportPersistenceAdapter implements HealthReportRepository {

    private final HealthReportJpaRepository jpaRepository;

    public HealthReportPersistenceAdapter(HealthReportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(HealthReport report) {
        jpaRepository.save(HealthReportEntity.from(report));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTelemetryId(UUID telemetryId) {
        return jpaRepository.existsByTelemetryId(telemetryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HealthReport> findLatestByVin(Vin vin) {
        return jpaRepository.findFirstByVinOrderByTelemetryRecordedAtDescAnalyzedAtDesc(vin.value())
                .map(HealthReportEntity::toDomain);
    }
}
