package com.renault.vehiclehealth.analysis.domain.scoring;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.renault.vehiclehealth.analysis.domain.scoring.TelemetryBuilder.aHealthyVehicle;
import static org.assertj.core.api.Assertions.assertThat;

class MileageRuleTest {

    private final MileageRule rule = new MileageRule();

    @ParameterizedTest(name = "{0} km -> penalty {1}, alerts {2}")
    @CsvSource({
            "50000,  0,  0",
            "100000, 0,  0",
            "100001, 5,  0",
            "150001, 10, 1",
            "200001, 15, 1"
    })
    void appliesPenaltyByMileageTier(long mileage, int expectedPenalty, int expectedAlerts) {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle().mileageKm(mileage).build());

        assertThat(outcome.penalty()).isEqualTo(expectedPenalty);
        assertThat(outcome.alerts()).hasSize(expectedAlerts);
    }
}
