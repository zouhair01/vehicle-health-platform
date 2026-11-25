package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.TirePressure;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TirePressureRule implements HealthRule {

    static final double CRITICAL_LOW_BAR = 1.5;
    static final double CRITICAL_HIGH_BAR = 3.5;
    static final double WARNING_LOW_BAR = 1.9;
    static final double WARNING_HIGH_BAR = 3.0;
    static final int CRITICAL_PENALTY = 10;
    static final int WARNING_PENALTY = 5;

    @Override
    public RuleOutcome evaluate(VehicleCondition condition) {
        TirePressure pressure = condition.telemetry().tirePressure();
        List<Map.Entry<String, Double>> tires = List.of(
                Map.entry("front left", pressure.frontLeftBar()),
                Map.entry("front right", pressure.frontRightBar()),
                Map.entry("rear left", pressure.rearLeftBar()),
                Map.entry("rear right", pressure.rearRightBar()));

        int penalty = 0;
        List<Alert> alerts = new ArrayList<>();
        for (Map.Entry<String, Double> tire : tires) {
            double bar = tire.getValue();
            if (bar < CRITICAL_LOW_BAR || bar > CRITICAL_HIGH_BAR) {
                penalty += CRITICAL_PENALTY;
                alerts.add(alert(AlertSeverity.CRITICAL, tire.getKey(), bar, "outside safe range"));
            } else if (bar < WARNING_LOW_BAR || bar > WARNING_HIGH_BAR) {
                penalty += WARNING_PENALTY;
                alerts.add(alert(AlertSeverity.WARNING, tire.getKey(), bar, "outside recommended range"));
            }
        }
        return new RuleOutcome(penalty, alerts);
    }

    private static Alert alert(AlertSeverity severity, String position, double bar, String reason) {
        String message = String.format(Locale.ROOT, "Tire %s pressure %.1f bar is %s", position, bar, reason);
        return new Alert(AlertType.TIRE_PRESSURE, severity, message);
    }
}
