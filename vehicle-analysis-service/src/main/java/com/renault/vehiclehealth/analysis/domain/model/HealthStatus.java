package com.renault.vehiclehealth.analysis.domain.model;

import java.util.List;

public enum HealthStatus {
    GOOD,
    ATTENTION_REQUIRED,
    CRITICAL;

    static final int GOOD_THRESHOLD = 80;
    static final int ATTENTION_THRESHOLD = 50;

    public static HealthStatus of(int score, List<Alert> alerts) {
        HealthStatus fromScore = score >= GOOD_THRESHOLD ? GOOD
                : score >= ATTENTION_THRESHOLD ? ATTENTION_REQUIRED
                : CRITICAL;
        boolean hasCriticalAlert = alerts.stream().anyMatch(alert -> alert.severity() == AlertSeverity.CRITICAL);
        return fromScore == GOOD && hasCriticalAlert ? ATTENTION_REQUIRED : fromScore;
    }
}
