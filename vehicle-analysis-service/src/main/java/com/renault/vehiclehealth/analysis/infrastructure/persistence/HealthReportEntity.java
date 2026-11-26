package com.renault.vehiclehealth.analysis.infrastructure.persistence;

import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.HealthStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "health_report")
public class HealthReportEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false, length = 17)
    private String vin;

    @Column(nullable = false)
    private UUID telemetryId;

    @Column(nullable = false)
    private Instant telemetryRecordedAt;

    @Column(nullable = false)
    private Instant analyzedAt;

    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HealthStatus status;

    @ElementCollection
    @CollectionTable(name = "health_alert", joinColumns = @JoinColumn(name = "report_id"))
    @OrderColumn(name = "alert_index")
    private List<AlertEmbeddable> alerts = new ArrayList<>();

    @Transient
    private boolean isNew = true;

    protected HealthReportEntity() {
    }

    static HealthReportEntity from(HealthReport report) {
        HealthReportEntity entity = new HealthReportEntity();
        entity.id = report.id();
        entity.vin = report.vin().value();
        entity.telemetryId = report.telemetryId();
        entity.telemetryRecordedAt = report.telemetryRecordedAt();
        entity.analyzedAt = report.analyzedAt();
        entity.score = report.assessment().score();
        entity.status = report.assessment().status();
        entity.alerts = new ArrayList<>(report.assessment().alerts().stream().map(AlertEmbeddable::from).toList());
        return entity;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        isNew = false;
    }
}
