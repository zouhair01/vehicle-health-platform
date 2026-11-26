package com.renault.vehiclehealth.analysis.infrastructure.persistence;

import com.renault.vehiclehealth.analysis.TestcontainersConfiguration;
import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.HealthAssessment;
import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.HealthStatus;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class HealthReportPersistenceAdapterTest {

    private static final Vin VIN = new Vin("VF1RFB00123456789");

    @Autowired
    private HealthReportPersistenceAdapter adapter;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void cleanDatabase() {
        jdbcClient.sql("DELETE FROM health_report").update();
    }

    @Test
    void findsReportOfMostRecentTelemetryWithItsAlertsInOrder() {
        List<Alert> alerts = List.of(
                new Alert(AlertType.ERROR_CODE, AlertSeverity.WARNING, "Diagnostic trouble code P0300 reported"),
                new Alert(AlertType.RECALL_CAMPAIGN, AlertSeverity.CRITICAL, "Open recall campaign RC-2026-014"));
        HealthReport older = report(VIN, "2026-11-25T08:00:00Z", 100, List.of());
        HealthReport latest = report(VIN, "2026-11-25T10:00:00Z", 75, alerts);
        HealthReport otherVehicle = report(new Vin("VF1AAA00000000001"), "2026-11-25T11:00:00Z", 50, List.of());
        adapter.save(latest);
        adapter.save(older);
        adapter.save(otherVehicle);

        assertThat(adapter.findLatestByVin(VIN)).hasValueSatisfying(found -> {
            assertThat(found.id()).isEqualTo(latest.id());
            assertThat(found.assessment().score()).isEqualTo(75);
            assertThat(found.assessment().alerts()).containsExactlyElementsOf(alerts);
        });
    }

    @Test
    void returnsEmptyWhenVehicleHasNoReport() {
        assertThat(adapter.findLatestByVin(VIN)).isEmpty();
    }

    private static HealthReport report(Vin vin, String recordedAt, int score, List<Alert> alerts) {
        return new HealthReport(UUID.randomUUID(), vin, UUID.randomUUID(), Instant.parse(recordedAt),
                Instant.parse("2026-11-25T12:00:00Z"),
                new HealthAssessment(score, HealthStatus.of(score, alerts), alerts));
    }
}
