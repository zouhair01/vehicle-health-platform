package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;

import java.util.Locale;

public final class FuelLevelRule implements HealthRule {

    static final double LOW_FUEL_PERCENT = 10;

    @Override
    public RuleOutcome evaluate(VehicleCondition condition) {
        double fuelLevel = condition.telemetry().fuelLevelPercent();
        if (fuelLevel < LOW_FUEL_PERCENT) {
            String message = String.format(Locale.ROOT, "Fuel level %.0f %% is low", fuelLevel);
            return RuleOutcome.of(5, new Alert(AlertType.LOW_FUEL, AlertSeverity.WARNING, message));
        }
        return RuleOutcome.none();
    }
}
