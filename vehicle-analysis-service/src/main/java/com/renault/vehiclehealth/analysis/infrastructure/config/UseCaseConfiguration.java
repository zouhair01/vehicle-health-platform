package com.renault.vehiclehealth.analysis.infrastructure.config;

import com.renault.vehiclehealth.analysis.application.AnalyzeVehicleUseCase;
import com.renault.vehiclehealth.analysis.domain.port.HealthReportRepository;
import com.renault.vehiclehealth.analysis.domain.port.RecallCampaignProvider;
import com.renault.vehiclehealth.analysis.domain.scoring.HealthScoreCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UseCaseConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    HealthScoreCalculator healthScoreCalculator() {
        return HealthScoreCalculator.withDefaultRules();
    }

    @Bean
    AnalyzeVehicleUseCase analyzeVehicleUseCase(RecallCampaignProvider recallCampaignProvider,
                                                HealthReportRepository healthReportRepository,
                                                HealthScoreCalculator healthScoreCalculator,
                                                Clock clock) {
        return new AnalyzeVehicleUseCase(recallCampaignProvider, healthReportRepository, healthScoreCalculator, clock);
    }
}
