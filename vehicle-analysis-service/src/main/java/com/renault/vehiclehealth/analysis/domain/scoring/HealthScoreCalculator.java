package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.HealthAssessment;
import com.renault.vehiclehealth.analysis.domain.model.HealthStatus;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;

import java.util.ArrayList;
import java.util.List;

public class HealthScoreCalculator {

    static final int INITIAL_SCORE = 100;

    private final List<HealthRule> rules;

    public HealthScoreCalculator(List<HealthRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public static HealthScoreCalculator withDefaultRules() {
        return new HealthScoreCalculator(List.of(
                new MileageRule(),
                new TirePressureRule(),
                new BatteryRule(),
                new EngineTemperatureRule(),
                new FuelLevelRule(),
                new ErrorCodeRule(),
                new RecallCampaignRule()));
    }

    public HealthAssessment assess(VehicleCondition condition) {
        int totalPenalty = 0;
        List<Alert> alerts = new ArrayList<>();
        for (HealthRule rule : rules) {
            RuleOutcome outcome = rule.evaluate(condition);
            totalPenalty += outcome.penalty();
            alerts.addAll(outcome.alerts());
        }
        int score = Math.max(0, INITIAL_SCORE - totalPenalty);
        return new HealthAssessment(score, HealthStatus.of(score, alerts), alerts);
    }
}
