package com.renault.vehiclehealth.analysis.domain.scoring;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.renault.vehiclehealth.analysis.domain.scoring.TelemetryBuilder.aHealthyVehicle;
import static org.assertj.core.api.Assertions.assertThat;

class EngineTemperatureRuleTest {

    private final EngineTemperatureRule rule = new EngineTemperatureRule();

    @ParameterizedTest(name = "{0} °C -> penalty {1}, severity {2}")
    @CsvSource({
            "90,  0,  -",
            "105, 0,  -",
            "106, 10, WARNING",
            "115, 10, WARNING",
            "116, 25, CRITICAL"
    })
    void appliesPenaltyByEngineTemperature(double temperature, int expectedPenalty, String expectedSeverity) {
        RuleOutcome outcome = rule.evaluate(aHealthyVehicle().engineTemperature(temperature).build());

        assertThat(outcome.penalty()).isEqualTo(expectedPenalty);
        assertThat(outcome.alerts().stream().map(alert -> alert.severity().name()).findFirst().orElse("-"))
                .isEqualTo(expectedSeverity);
    }
}
