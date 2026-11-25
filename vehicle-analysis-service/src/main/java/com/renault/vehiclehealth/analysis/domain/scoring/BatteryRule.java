package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;

import java.util.Locale;

public final class BatteryRule implements HealthRule {

    static final double CRITICAL_VOLTAGE = 11.8;
    static final double WARNING_VOLTAGE = 12.2;

    @Override
    public RuleOutcome evaluate(VehicleCondition condition) {
        double voltage = condition.telemetry().batteryVoltage();
        if (voltage < CRITICAL_VOLTAGE) {
            return RuleOutcome.of(20, alert(AlertSeverity.CRITICAL, voltage, "critically low"));
        }
        if (voltage < WARNING_VOLTAGE) {
            return RuleOutcome.of(10, alert(AlertSeverity.WARNING, voltage, "low"));
        }
        return RuleOutcome.none();
    }

    private static Alert alert(AlertSeverity severity, double voltage, String level) {
        String message = String.format(Locale.ROOT, "Battery voltage %.1f V is %s", voltage, level);
        return new Alert(AlertType.BATTERY, severity, message);
    }
}
