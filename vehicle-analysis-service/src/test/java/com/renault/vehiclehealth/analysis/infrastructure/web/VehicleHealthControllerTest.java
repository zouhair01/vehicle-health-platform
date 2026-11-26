package com.renault.vehiclehealth.analysis.infrastructure.web;

import com.renault.vehiclehealth.analysis.application.GetLatestHealthReportUseCase;
import com.renault.vehiclehealth.analysis.application.GetVehicleAlertsUseCase;
import com.renault.vehiclehealth.analysis.application.HealthReportNotFoundException;
import com.renault.vehiclehealth.analysis.application.VehicleAlerts;
import com.renault.vehiclehealth.analysis.domain.model.Alert;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.AlertType;
import com.renault.vehiclehealth.analysis.domain.model.HealthAssessment;
import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.HealthStatus;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleHealthController.class)
class VehicleHealthControllerTest {

    private static final Vin VIN = new Vin("VF1RFB00123456789");
    private static final Alert ERROR_CODE_ALERT =
            new Alert(AlertType.ERROR_CODE, AlertSeverity.WARNING, "Diagnostic trouble code P0300 reported");
    private static final Alert RECALL_ALERT =
            new Alert(AlertType.RECALL_CAMPAIGN, AlertSeverity.CRITICAL, "Open recall campaign RC-2026-014");
    private static final HealthReport REPORT = new HealthReport(
            UUID.fromString("7c1e2f30-1111-4a2b-9c3d-123456789abc"), VIN,
            UUID.fromString("042e3dd6-ed45-4795-bbcb-c3c67b0dc34f"),
            Instant.parse("2026-11-25T10:00:00Z"), Instant.parse("2026-11-25T10:05:00Z"),
            new HealthAssessment(75, HealthStatus.ATTENTION_REQUIRED, List.of(ERROR_CODE_ALERT, RECALL_ALERT)));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetLatestHealthReportUseCase getLatestHealthReport;

    @MockitoBean
    private GetVehicleAlertsUseCase getVehicleAlerts;

    @Test
    void returnsLatestHealthReport() throws Exception {
        when(getLatestHealthReport.getLatest(VIN)).thenReturn(REPORT);

        mockMvc.perform(get("/api/v1/vehicles/{vin}/health-report", VIN.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value(VIN.value()))
                .andExpect(jsonPath("$.score").value(75))
                .andExpect(jsonPath("$.status").value("ATTENTION_REQUIRED"))
                .andExpect(jsonPath("$.telemetryId").value("042e3dd6-ed45-4795-bbcb-c3c67b0dc34f"))
                .andExpect(jsonPath("$.analyzedAt").value("2026-11-25T10:05:00Z"))
                .andExpect(jsonPath("$.alerts.length()").value(2))
                .andExpect(jsonPath("$.alerts[1].type").value("RECALL_CAMPAIGN"))
                .andExpect(jsonPath("$.alerts[1].severity").value("CRITICAL"));
    }

    @Test
    void returnsNotFoundWhenVehicleHasNoReport() throws Exception {
        when(getLatestHealthReport.getLatest(any())).thenThrow(new HealthReportNotFoundException(VIN));

        mockMvc.perform(get("/api/v1/vehicles/{vin}/health-report", VIN.value()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Health report not found"));
    }

    @Test
    void returnsBadRequestWhenVinIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/{vin}/health-report", "INVALID-VIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid VIN"));

        verifyNoInteractions(getLatestHealthReport);
    }

    @Test
    void returnsAlertsOfLatestReport() throws Exception {
        when(getVehicleAlerts.getAlerts(VIN, null)).thenReturn(
                new VehicleAlerts(VIN, REPORT.id(), REPORT.analyzedAt(), List.of(ERROR_CODE_ALERT, RECALL_ALERT)));

        mockMvc.perform(get("/api/v1/vehicles/{vin}/alerts", VIN.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value(VIN.value()))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.alerts[0].type").value("ERROR_CODE"))
                .andExpect(jsonPath("$.alerts[0].message").value("Diagnostic trouble code P0300 reported"));
    }

    @Test
    void passesSeverityFilterToUseCase() throws Exception {
        when(getVehicleAlerts.getAlerts(VIN, AlertSeverity.CRITICAL)).thenReturn(
                new VehicleAlerts(VIN, REPORT.id(), REPORT.analyzedAt(), List.of(RECALL_ALERT)));

        mockMvc.perform(get("/api/v1/vehicles/{vin}/alerts", VIN.value()).param("severity", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

        verify(getVehicleAlerts).getAlerts(VIN, AlertSeverity.CRITICAL);
    }

    @Test
    void returnsBadRequestWhenSeverityIsUnknown() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/{vin}/alerts", VIN.value()).param("severity", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getVehicleAlerts);
    }
}
