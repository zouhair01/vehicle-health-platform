package com.renault.vehiclehealth.analysis.infrastructure.persistence;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class AlertEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlertSeverity severity;

    @Column(nullable = false)
    private String message;

    protected AlertEmbeddable() {
    }

    static AlertEmbeddable from(Alert alert) {
        AlertEmbeddable embeddable = new AlertEmbeddable();
        embeddable.type = alert.type();
        embeddable.severity = alert.severity();
        embeddable.message = alert.message();
        return embeddable;
    }
}
