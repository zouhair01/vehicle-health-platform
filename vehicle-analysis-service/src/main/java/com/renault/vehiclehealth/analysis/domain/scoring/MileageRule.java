package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;

public final class MileageRule implements HealthRule {

    static final long MODERATE_MILEAGE_KM = 100_000;
    static final long HIGH_MILEAGE_KM = 150_000;
    static final long VERY_HIGH_MILEAGE_KM = 200_000;

    @Override
    public RuleOutcome evaluate(VehicleCondition condition) {
        long mileage = condition.telemetry().mileageKm();
        if (mileage > VERY_HIGH_MILEAGE_KM) {
            return RuleOutcome.of(15, warning("Mileage above " + VERY_HIGH_MILEAGE_KM + " km: major service recommended"));
        }
        if (mileage > HIGH_MILEAGE_KM) {
            return RuleOutcome.of(10, warning("Mileage above " + HIGH_MILEAGE_KM + " km: wear parts inspection recommended"));
        }
        if (mileage > MODERATE_MILEAGE_KM) {
            return RuleOutcome.penaltyOnly(5);
        }
        return RuleOutcome.none();
    }

    private static Alert warning(String message) {
        return new Alert(AlertType.HIGH_MILEAGE, AlertSeverity.WARNING, message);
    }
}
