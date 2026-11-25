package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.RecallCampaign;
import org.junit.jupiter.api.Test;

import static com.renault.vehiclehealth.analysis.domain.scoring.TelemetryBuilder.aHealthyVehicle;
import static org.assertj.core.api.Assertions.assertThat;

class RecallCampaignRuleTest {

    private final RecallCampaignRule rule = new RecallCampaignRule();

    @Test
    void criticalAlertForEachOpenCampaign() {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle()
                .recallCampaigns(new RecallCampaign("RC-2026-014", "Brake hose inspection"))
                .build());

        assertThat(outcome.penalty()).isEqualTo(15);
        assertThat(outcome.alerts()).singleElement()
                .satisfies(alert -> {
                    assertThat(alert.severity()).isEqualTo(AlertSeverity.CRITICAL);
                    assertThat(alert.message()).contains("RC-2026-014");
                });
    }

    @Test
    void capsPenalty() {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle().recallCampaigns(
                new RecallCampaign("RC-1", "a"), new RecallCampaign("RC-2", "b"), new RecallCampaign("RC-3", "c"))
                .build());

        assertThat(outcome.penalty()).isEqualTo(30);
        assertThat(outcome.alerts()).hasSize(3);
    }
}
