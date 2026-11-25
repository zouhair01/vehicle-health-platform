package com.renault.vehiclehealth.analysis.domain.scoring;

import org.junit.jupiter.api.Test;

import static com.renault.vehiclehealth.analysis.domain.scoring.TelemetryBuilder.aHealthyVehicle;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeRuleTest {

    private final ErrorCodeRule rule = new ErrorCodeRule();

    @Test
    void noPenaltyWithoutErrorCode() {
        assertThat(rule.evaluate(aHealthyVehicle().build())).isEqualTo(RuleOutcome.none());
    }

    @Test
    void penalizesEachDistinctCode() {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle().errorCodes("P0300", "P0171", "P0300").build());

        assertThat(outcome.penalty()).isEqualTo(20);
        assertThat(outcome.alerts()).hasSize(2);
    }

    @Test
    void capsPenalty() {
        RuleOutcome outcome = rule.evaluate(
                aHealthyVehicle().errorCodes("P0300", "P0171", "P0420", "C0035", "U0100").build());

        assertThat(outcome.penalty()).isEqualTo(30);
        assertThat(outcome.alerts()).hasSize(5);
    }
}
