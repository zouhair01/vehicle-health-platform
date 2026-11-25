package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import org.junit.jupiter.api.Test;

import static com.renault.vehiclehealth.analysis.domain.scoring.TelemetryBuilder.aHealthyVehicle;
import static org.assertj.core.api.Assertions.assertThat;

class TirePressureRuleTest {

    private final TirePressureRule rule = new TirePressureRule();

    @Test
    void noPenaltyWhenAllTiresAreInRecommendedRange() {
        assertThat(rule.evaluate(aHealthyVehicle().build())).isEqualTo(RuleOutcome.none());
    }

    @Test
    void warningWhenOneTireIsOutsideRecommendedRange() {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle().tirePressure(1.7, 2.4, 2.4, 2.4).build());

        assertThat(outcome.penalty()).isEqualTo(5);
        assertThat(outcome.alerts()).singleElement()
                .satisfies(alert -> {
                    assertThat(alert.severity()).isEqualTo(AlertSeverity.WARNING);
                    assertThat(alert.message()).contains("front left").contains("1.7");
                });
    }

    @Test
    void criticalWhenOneTireIsOutsideSafeRange() {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle().tirePressure(2.4, 2.4, 2.4, 3.8).build());

        assertThat(outcome.penalty()).isEqualTo(10);
        assertThat(outcome.alerts()).singleElement()
                .satisfies(alert -> assertThat(alert.severity()).isEqualTo(AlertSeverity.CRITICAL));
    }

    @Test
    void penaltiesAddUpForEachTire() {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle().tirePressure(1.2, 1.2, 1.2, 1.2).build());

        assertThat(outcome.penalty()).isEqualTo(40);
        assertThat(outcome.alerts()).hasSize(4);
    }
}
