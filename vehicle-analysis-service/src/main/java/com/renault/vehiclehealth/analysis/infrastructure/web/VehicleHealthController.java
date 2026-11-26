package com.renault.vehiclehealth.analysis.infrastructure.web;

import com.renault.vehiclehealth.analysis.application.GetLatestHealthReportUseCase;
import com.renault.vehiclehealth.analysis.application.GetVehicleAlertsUseCase;
import com.renault.vehiclehealth.analysis.domain.model.AlertSeverity;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles/{vin}")
@Tag(name = "Vehicle health", description = "Health reports and alerts")
public class VehicleHealthController {

    private final GetLatestHealthReportUseCase getLatestHealthReport;
    private final GetVehicleAlertsUseCase getVehicleAlerts;

    public VehicleHealthController(GetLatestHealthReportUseCase getLatestHealthReport,
                                   GetVehicleAlertsUseCase getVehicleAlerts) {
        this.getLatestHealthReport = getLatestHealthReport;
        this.getVehicleAlerts = getVehicleAlerts;
    }

    @GetMapping("/health-report")
    @Operation(summary = "Get the latest health report",
            description = "Returns the report computed from the most recently recorded telemetry of the vehicle.")
    @ApiResponse(responseCode = "200", description = "Health report found")
    @ApiResponse(responseCode = "400", description = "Invalid VIN",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No health report for this vehicle",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public HealthReportResponse getHealthReport(
            @Parameter(description = "Vehicle Identification Number (17 characters)", example = "VF1RFB00123456789")
            @PathVariable String vin) {
        return HealthReportResponse.from(getLatestHealthReport.getLatest(new Vin(vin)));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get the alerts of the latest health report",
            description = "Optionally filtered by severity.")
    @ApiResponse(responseCode = "200", description = "Alerts returned (possibly empty)")
    @ApiResponse(responseCode = "400", description = "Invalid VIN or unknown severity",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No health report for this vehicle",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public VehicleAlertsResponse getAlerts(
            @Parameter(description = "Vehicle Identification Number (17 characters)", example = "VF1RFB00123456789")
            @PathVariable String vin,
            @Parameter(description = "Keep only alerts of this severity")
            @RequestParam(required = false) AlertSeverity severity) {
        return VehicleAlertsResponse.from(getVehicleAlerts.getAlerts(new Vin(vin), severity));
    }
}
