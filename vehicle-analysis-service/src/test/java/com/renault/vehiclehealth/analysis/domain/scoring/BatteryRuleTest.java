package com.renault.vehiclehealth.analysis.domain.scoring;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.renault.vehiclehealth.analysis.domain.scoring.TelemetryBuilder.aHealthyVehicle;
import static org.assertj.core.api.Assertions.assertThat;

class BatteryRuleTest {

    private final BatteryRule rule = new BatteryRule();

    @ParameterizedTest(name = "{0} V -> penalty {1}, severity {2}")
    @CsvSource({
            "12.6, 0,  -",
            "12.2, 0,  -",
            "12.1, 10, WARNING",
            "11.8, 10, WARNING",
            "11.7, 20, CRITICAL"
    })
    void appliesPenaltyByVoltage(double voltage, int expectedPenalty, String expectedSeverity) {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle().batteryVoltage(voltage).build());

        assertThat(outcome.penalty()).isEqualTo(expectedPenalty);
        assertThat(outcome.alerts().stream().map(alert -> alert.severity().name()).findFirst().orElse("-"))
                .isEqualTo(expectedSeverity);
    }
}
