package com.renault.vehiclehealth.analysis.application;

import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.HealthAssessment;
import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.HealthStatus;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import com.renault.vehiclehealth.analysis.domain.port.HealthReportRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HealthReportQueriesTest {

    private static final Vin VIN = new Vin("VF1RFB00123456789");
    private static final Vin UNKNOWN_VIN = new Vin("VF1AAA00000000001");

    private static final Alert ERROR_CODE_ALERT =
            new Alert(AlertType.ERROR_CODE, AlertSeverity.WARNING, "Diagnostic trouble code P0300 reported");
    private static final Alert RECALL_ALERT =
            new Alert(AlertType.RECALL_CAMPAIGN, AlertSeverity.CRITICAL, "Open recall campaign RC-2026-014");

    private static final HealthReport REPORT = new HealthReport(
            UUID.randomUUID(), VIN, UUID.randomUUID(), Instant.parse("2026-11-25T10:00:00Z"),
            Instant.parse("2026-11-25T10:05:00Z"),
            new HealthAssessment(75, HealthStatus.ATTENTION_REQUIRED, List.of(ERROR_CODE_ALERT, RECALL_ALERT)));

    private final HealthReportRepository repository = new HealthReportRepository() {
        @Override
        public void save(HealthReport report) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByTelemetryId(UUID telemetryId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<HealthReport> findLatestByVin(Vin vin) {
            return VIN.equals(vin) ? Optional.of(REPORT) : Optional.empty();
        }
    };

    private final GetLatestHealthReportUseCase getLatestHealthReport = new GetLatestHealthReportUseCase(repository);
    private final GetVehicleAlertsUseCase getVehicleAlerts = new GetVehicleAlertsUseCase(repository);

    @Test
    void returnsLatestReport() {
        assertThat(getLatestHealthReport.getLatest(VIN)).isEqualTo(REPORT);
    }

    @Test
    void failsWhenVehicleHasNoReport() {
        assertThatThrownBy(() -> getLatestHealthReport.getLatest(UNKNOWN_VIN))
                .isInstanceOf(HealthReportNotFoundException.class)
                .hasMessageContaining(UNKNOWN_VIN.value());
    }

    @Test
    void returnsAllAlertsWithoutFilter() {
        VehicleAlerts alerts = getVehicleAlerts.getAlerts(VIN, null);

        assertThat(alerts.reportId()).isEqualTo(REPORT.id());
        assertThat(alerts.alerts()).containsExactly(ERROR_CODE_ALERT, RECALL_ALERT);
    }

    @Test
    void filtersAlertsBySeverity() {
        assertThat(getVehicleAlerts.getAlerts(VIN, AlertSeverity.CRITICAL).alerts()).containsExactly(RECALL_ALERT);
    }

    @Test
    void alertsFailWhenVehicleHasNoReport() {
        assertThatThrownBy(() -> getVehicleAlerts.getAlerts(UNKNOWN_VIN, null))
                .isInstanceOf(HealthReportNotFoundException.class);
    }
}
