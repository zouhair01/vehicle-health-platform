package com.renault.vehiclehealth.analysis.domain.model;

import java.util.List;
import java.util.Objects;

public record HealthAssessment(int score, HealthStatus status, List<Alert> alerts) {

    public HealthAssessment {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
        Objects.requireNonNull(status, "status is required");
        alerts = List.copyOf(alerts);
    }
}
