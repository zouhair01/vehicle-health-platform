package com.renault.vehiclehealth.telemetry.application;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.model.TelemetryReading;
import com.renault.vehiclehealth.telemetry.domain.port.TelemetryEventPublisher;
import com.renault.vehiclehealth.telemetry.domain.port.TelemetryRepository;

import java.time.Clock;
import java.util.UUID;

public class ReceiveTelemetryUseCase {

    private final TelemetryRepository telemetryRepository;
    private final TelemetryEventPublisher telemetryEventPublisher;
    private final Clock clock;

    public ReceiveTelemetryUseCase(TelemetryRepository telemetryRepository,
                                   TelemetryEventPublisher telemetryEventPublisher,
                                   Clock clock) {
        this.telemetryRepository = telemetryRepository;
        this.telemetryEventPublisher = telemetryEventPublisher;
        this.clock = clock;
    }

    public Telemetry receive(TelemetryReading reading) {
        Telemetry telemetry = new Telemetry(UUID.randomUUID(), reading, clock.instant());
        telemetryRepository.save(telemetry);
        telemetryEventPublisher.publishTelemetryReceived(telemetry);
        return telemetry;
    }
}
