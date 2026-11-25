package com.renault.vehiclehealth.analysis.domain.scoring;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.renault.vehiclehealth.analysis.domain.scoring.TelemetryBuilder.aHealthyVehicle;
import static org.assertj.core.api.Assertions.assertThat;

class FuelLevelRuleTest {

    private final FuelLevelRule rule = new FuelLevelRule();

    @ParameterizedTest(name = "{0} % -> penalty {1}, alerts {2}")
    @CsvSource({
            "60, 0, 0",
            "10, 0, 0",
            "9,  5, 1"
    })
    void appliesPenaltyWhenFuelIsLow(double fuelLevel, int expectedPenalty, int expectedAlerts) {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle().fuelLevel(fuelLevel).build());

        assertThat(outcome.penalty()).isEqualTo(expectedPenalty);
        assertThat(outcome.alerts()).hasSize(expectedAlerts);
    }
}
