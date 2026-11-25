package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.Alert;

import java.util.List;

public record RuleOutcome(int penalty, List<Alert> alerts) {

    public RuleOutcome {
        if (penalty < 0) {
            throw new IllegalArgumentException("penalty must be positive or zero");
        }
        alerts = List.copyOf(alerts);
    }

    public static RuleOutcome none() {
        return new RuleOutcome(0, List.of());
    }

    public static RuleOutcome penaltyOnly(int penalty) {
        return new RuleOutcome(penalty, List.of());
    }

    public static RuleOutcome of(int penalty, Alert alert) {
        return new RuleOutcome(penalty, List.of(alert));
    }
}
