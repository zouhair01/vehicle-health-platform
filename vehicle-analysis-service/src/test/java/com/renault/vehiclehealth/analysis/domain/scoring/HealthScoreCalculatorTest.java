package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.HealthAssessment;
import com.renault.vehiclehealth.analysis.domain.model.HealthStatus;
import com.renault.vehiclehealth.analysis.domain.model.RecallCampaign;
import org.junit.jupiter.api.Test;

import static com.renault.vehiclehealth.analysis.domain.scoring.TelemetryBuilder.aHealthyVehicle;
import static org.assertj.core.api.Assertions.assertThat;

class HealthScoreCalculatorTest {

    private final HealthScoreCalculator calculator = HealthScoreCalculator.withDefaultRules();

    @Test
    void healthyVehicleKeepsInitialScore() {
        HealthAssessment assessment = calculator.assess(aHealthyVehicle().build());

        assertThat(assessment.score()).isEqualTo(100);
        assertThat(assessment.status()).isEqualTo(HealthStatus.GOOD);
        assertThat(assessment.alerts()).isEmpty();
    }

    @Test
    void combinesPenaltiesFromAllRules() {
        HealthAssessment assessment = calculator.assess(aHealthyVehicle()
                .mileageKm(160_000)
                .batteryVoltage(12.0)
                .errorCodes("P0300")
                .recallCampaigns(new RecallCampaign("RC-2026-014", "Brake hose inspection"))
                .build());

        assertThat(assessment.score()).isEqualTo(100 - 10 - 10 - 10 - 15);
        assertThat(assessment.status()).isEqualTo(HealthStatus.ATTENTION_REQUIRED);
        assertThat(assessment.alerts()).extracting(alert -> alert.type()).containsExactly(
                AlertType.HIGH_MILEAGE, AlertType.BATTERY, AlertType.ERROR_CODE, AlertType.RECALL_CAMPAIGN);
    }

    @Test
    void scoreNeverGoesBelowZero() {
        HealthAssessment assessment = calculator.assess(aHealthyVehicle()
                .mileageKm(250_000)
                .tirePressure(1.0, 1.0, 1.0, 1.0)
                .batteryVoltage(10.5)
                .engineTemperature(125)
                .fuelLevel(2)
                .errorCodes("P0300", "P0171", "P0420")
                .recallCampaigns(new RecallCampaign("RC-1", "a"), new RecallCampaign("RC-2", "b"))
                .build());

        assertThat(assessment.score()).isZero();
        assertThat(assessment.status()).isEqualTo(HealthStatus.CRITICAL);
    }

    @Test
    void criticalAlertPreventsGoodStatus() {
        HealthAssessment assessment = calculator.assess(aHealthyVehicle().tirePressure(1.2, 2.4, 2.4, 2.4).build());

        assertThat(assessment.score()).isEqualTo(90);
        assertThat(assessment.status()).isEqualTo(HealthStatus.ATTENTION_REQUIRED);
    }
}
