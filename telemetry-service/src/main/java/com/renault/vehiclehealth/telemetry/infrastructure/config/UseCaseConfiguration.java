package com.renault.vehiclehealth.telemetry.infrastructure.config;

import com.renault.vehiclehealth.telemetry.application.ReceiveTelemetryUseCase;
import com.renault.vehiclehealth.telemetry.domain.port.TelemetryRepository;
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
    ReceiveTelemetryUseCase receiveTelemetryUseCase(TelemetryRepository telemetryRepository, Clock clock) {
        return new ReceiveTelemetryUseCase(telemetryRepository, clock);
    }
}
