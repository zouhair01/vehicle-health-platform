package com.renault.vehiclehealth.telemetry.application;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.model.TelemetryReading;
import com.renault.vehiclehealth.telemetry.domain.model.TirePressure;
import com.renault.vehiclehealth.telemetry.domain.model.Vin;
import com.renault.vehiclehealth.telemetry.domain.port.TelemetryEventPublisher;
import com.renault.vehiclehealth.telemetry.domain.port.TelemetryRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiveTelemetryUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-11-25T10:05:00Z");

    private final List<String> calls = new ArrayList<>();
    private final List<Telemetry> saved = new ArrayList<>();
    private final List<Telemetry> published = new ArrayList<>();

    private final TelemetryRepository repository = telemetry -> {
        calls.add("save");
        saved.add(telemetry);
    };
    private final TelemetryEventPublisher publisher = telemetry -> {
        calls.add("publish");
        published.add(telemetry);
    };

    private final ReceiveTelemetryUseCase useCase =
            new ReceiveTelemetryUseCase(repository, publisher, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void storesTelemetryWithGeneratedIdAndReceptionTime() {
        Telemetry telemetry = useCase.receive(reading());

        assertThat(telemetry.id()).isNotNull();
        assertThat(telemetry.receivedAt()).isEqualTo(NOW);
        assertThat(saved).containsExactly(telemetry);
    }

    @Test
    void publishesTelemetryReceivedEventAfterStoringIt() {
        Telemetry telemetry = useCase.receive(reading());

        assertThat(published).containsExactly(telemetry);
        assertThat(calls).containsExactly("save", "publish");
    }

    private static TelemetryReading reading() {
        return new TelemetryReading(
                new Vin("VF1RFB00123456789"), Instant.parse("2026-11-25T10:00:00Z"), 85_000,
                new TirePressure(2.3, 2.3, 2.4, 2.4), 12.6, 90, 45, List.of("P0300"));
    }
}
