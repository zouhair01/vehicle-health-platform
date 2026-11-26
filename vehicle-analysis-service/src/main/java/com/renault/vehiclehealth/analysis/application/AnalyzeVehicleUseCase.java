package com.renault.vehiclehealth.analysis.application;

import com.renault.vehiclehealth.analysis.domain.model.HealthAssessment;
import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.RecallCampaign;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;
import com.renault.vehiclehealth.analysis.domain.model.VehicleTelemetry;
import com.renault.vehiclehealth.analysis.domain.port.HealthReportRepository;
import com.renault.vehiclehealth.analysis.domain.port.RecallCampaignProvider;
import com.renault.vehiclehealth.analysis.domain.scoring.HealthScoreCalculator;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AnalyzeVehicleUseCase {

    private final RecallCampaignProvider recallCampaignProvider;
    private final HealthReportRepository healthReportRepository;
    private final HealthScoreCalculator healthScoreCalculator;
    private final Clock clock;

    public AnalyzeVehicleUseCase(RecallCampaignProvider recallCampaignProvider,
                                 HealthReportRepository healthReportRepository,
                                 HealthScoreCalculator healthScoreCalculator,
                                 Clock clock) {
        this.recallCampaignProvider = recallCampaignProvider;
        this.healthReportRepository = healthReportRepository;
        this.healthScoreCalculator = healthScoreCalculator;
        this.clock = clock;
    }

    /**
     * @return the new report, or empty if this telemetry has already been analyzed (idempotent processing)
     */
    public Optional<HealthReport> analyze(VehicleTelemetry telemetry) {
        if (healthReportRepository.existsByTelemetryId(telemetry.telemetryId())) {
            return Optional.empty();
        }
        List<RecallCampaign> openCampaigns = recallCampaignProvider.findOpenCampaigns(telemetry.vin());
        HealthAssessment assessment = healthScoreCalculator.assess(new VehicleCondition(telemetry, openCampaigns));
        HealthReport report = new HealthReport(
                UUID.randomUUID(), telemetry.vin(), telemetry.telemetryId(),
                telemetry.recordedAt(), clock.instant(), assessment);
        healthReportRepository.save(report);
        return Optional.of(report);
    }
}
