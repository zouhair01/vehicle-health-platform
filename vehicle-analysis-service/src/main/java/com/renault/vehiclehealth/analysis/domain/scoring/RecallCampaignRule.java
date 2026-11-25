package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.RecallCampaign;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;

import java.util.List;

public final class RecallCampaignRule implements HealthRule {

    static final int PENALTY_PER_CAMPAIGN = 15;
    static final int MAX_PENALTY = 30;

    @Override
    public RuleOutcome evaluate(VehicleCondition condition) {
        List<RecallCampaign> campaigns = condition.openRecallCampaigns();
        List<Alert> alerts = campaigns.stream()
                .map(campaign -> new Alert(AlertType.RECALL_CAMPAIGN, AlertSeverity.CRITICAL,
                        "Open recall campaign " + campaign.code() + ": " + campaign.description()))
                .toList();
        int penalty = Math.min(campaigns.size() * PENALTY_PER_CAMPAIGN, MAX_PENALTY);
        return new RuleOutcome(penalty, alerts);
    }
}
