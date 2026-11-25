package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;

import java.util.List;

public final class ErrorCodeRule implements HealthRule {

    static final int PENALTY_PER_CODE = 10;
    static final int MAX_PENALTY = 30;

    @Override
    public RuleOutcome evaluate(VehicleCondition condition) {
        List<String> distinctCodes = condition.telemetry().errorCodes().stream().distinct().toList();
        List<Alert> alerts = distinctCodes.stream()
                .map(code -> new Alert(AlertType.ERROR_CODE, AlertSeverity.WARNING, "Diagnostic trouble code " + code + " reported"))
                .toList();
        int penalty = Math.min(distinctCodes.size() * PENALTY_PER_CODE, MAX_PENALTY);
        return new RuleOutcome(penalty, alerts);
    }
}
