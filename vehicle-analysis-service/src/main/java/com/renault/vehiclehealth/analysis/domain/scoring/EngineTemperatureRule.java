package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;

import java.util.Locale;

public final class EngineTemperatureRule implements HealthRule {

    static final double WARNING_TEMPERATURE = 105;
    static final double CRITICAL_TEMPERATURE = 115;

    @Override
    public RuleOutcome evaluate(VehicleCondition condition) {
        double temperature = condition.telemetry().engineTemperatureCelsius();
        if (temperature > CRITICAL_TEMPERATURE) {
            return RuleOutcome.of(25, alert(AlertSeverity.CRITICAL, temperature, "overheating"));
        }
        if (temperature > WARNING_TEMPERATURE) {
            return RuleOutcome.of(10, alert(AlertSeverity.WARNING, temperature, "above normal"));
        }
        return RuleOutcome.none();
    }

    private static Alert alert(AlertSeverity severity, double temperature, String level) {
        String message = String.format(Locale.ROOT, "Engine temperature %.0f °C is %s", temperature, level);
        return new Alert(AlertType.ENGINE_TEMPERATURE, severity, message);
    }
}
