package com.renault.vehiclehealth.analysis.application;

import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.HealthStatus;
import com.renault.vehiclehealth.analysis.domain.model.RecallCampaign;
import com.renault.vehiclehealth.analysis.domain.model.TirePressure;
import com.renault.vehiclehealth.analysis.domain.model.VehicleTelemetry;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import com.renault.vehiclehealth.analysis.domain.port.HealthReportRepository;
import com.renault.vehiclehealth.analysis.domain.port.RecallProviderUnavailableException;
import com.renault.vehiclehealth.analysis.domain.scoring.HealthScoreCalculator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalyzeVehicleUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-11-25T10:05:00Z");
    private static final Vin VIN = new Vin("VF1RFB00123456789");

    private final InMemoryReports reports = new InMemoryReports();
    private final AtomicInteger providerCalls = new AtomicInteger();

    @Test
    void computesAndStoresReportUsingOpenRecallCampaigns() {
        AnalyzeVehicleUseCase useCase = useCaseWithRecalls(List.of(new RecallCampaign("RC-2026-014", "Brake hose inspection")));
        VehicleTelemetry telemetry = telemetry(UUID.randomUUID(), List.of("P0300"));

        Optional<HealthReport> report = useCase.analyze(telemetry);

        assertThat(report).hasValueSatisfying(r -> {
            assertThat(r.vin()).isEqualTo(VIN);
            assertThat(r.telemetryId()).isEqualTo(telemetry.telemetryId());
            assertThat(r.analyzedAt()).isEqualTo(NOW);
            assertThat(r.assessment().score()).isEqualTo(100 - 10 - 15);
            assertThat(r.assessment().status()).isEqualTo(HealthStatus.ATTENTION_REQUIRED);
        });
        assertThat(reports.saved).hasSize(1);
    }

    @Test
    void skipsTelemetryAlreadyAnalyzed() {
        AnalyzeVehicleUseCase useCase = useCaseWithRecalls(List.of());
        VehicleTelemetry telemetry = telemetry(UUID.randomUUID(), List.of());
        useCase.analyze(telemetry);

        Optional<HealthReport> secondAttempt = useCase.analyze(telemetry);

        assertThat(secondAttempt).isEmpty();
        assertThat(reports.saved).hasSize(1);
        assertThat(providerCalls).hasValue(1);
    }

    @Test
    void storesNothingWhenRecallProviderIsUnavailable() {
        AnalyzeVehicleUseCase useCase = new AnalyzeVehicleUseCase(
                vin -> {
                    throw new RecallProviderUnavailableException("provider down", null);
                },
                reports, HealthScoreCalculator.withDefaultRules(), Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatThrownBy(() -> useCase.analyze(telemetry(UUID.randomUUID(), List.of())))
                .isInstanceOf(RecallProviderUnavailableException.class);
        assertThat(reports.saved).isEmpty();
    }

    private AnalyzeVehicleUseCase useCaseWithRecalls(List<RecallCampaign> campaigns) {
        return new AnalyzeVehicleUseCase(
                vin -> {
                    providerCalls.incrementAndGet();
                    return campaigns;
                },
                reports, HealthScoreCalculator.withDefaultRules(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static VehicleTelemetry telemetry(UUID telemetryId, List<String> errorCodes) {
        return new VehicleTelemetry(telemetryId, VIN, Instant.parse("2026-11-25T10:00:00Z"), 85_000,
                new TirePressure(2.3, 2.3, 2.4, 2.4), 12.6, 90, 45, errorCodes);
    }

    private static final class InMemoryReports implements HealthReportRepository {

        private final List<HealthReport> saved = new ArrayList<>();

        @Override
        public void save(HealthReport report) {
            saved.add(report);
        }

        @Override
        public boolean existsByTelemetryId(UUID telemetryId) {
            return saved.stream().anyMatch(report -> report.telemetryId().equals(telemetryId));
        }
    }
}
