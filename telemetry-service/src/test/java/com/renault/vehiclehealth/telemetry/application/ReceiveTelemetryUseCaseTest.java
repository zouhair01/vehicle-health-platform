package com.renault.vehiclehealth.telemetry.application;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.model.TelemetryReading;
import com.renault.vehiclehealth.telemetry.domain.model.TirePressure;
import com.renault.vehiclehealth.telemetry.domain.model.Vin;
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

    private final RecordingTelemetryRepository repository = new RecordingTelemetryRepository();
    private final ReceiveTelemetryUseCase useCase =
            new ReceiveTelemetryUseCase(repository, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void storesTelemetryWithGeneratedIdAndReceptionTime() {
        TelemetryReading reading = new TelemetryReading(
                new Vin("VF1RFB00123456789"), Instant.parse("2026-11-25T10:00:00Z"), 85_000,
                new TirePressure(2.3, 2.3, 2.4, 2.4), 12.6, 90, 45, List.of("P0300"));

        Telemetry telemetry = useCase.receive(reading);

        assertThat(telemetry.id()).isNotNull();
        assertThat(telemetry.receivedAt()).isEqualTo(NOW);
        assertThat(telemetry.reading()).isEqualTo(reading);
        assertThat(repository.saved).containsExactly(telemetry);
    }

    private static final class RecordingTelemetryRepository implements TelemetryRepository {

        private final List<Telemetry> saved = new ArrayList<>();

        @Override
        public void save(Telemetry telemetry) {
            saved.add(telemetry);
        }
    }
}
