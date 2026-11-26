package com.renault.vehiclehealth.analysis.infrastructure.web;

import com.renault.vehiclehealth.analysis.domain.model.HealthReport;
import com.renault.vehiclehealth.analysis.domain.model.HealthStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HealthReportResponse(
        String vin,
        int score,
        HealthStatus status,
        UUID reportId,
        UUID telemetryId,
        Instant telemetryRecordedAt,
        Instant analyzedAt,
        List<AlertResponse> alerts) {

    static HealthReportResponse from(HealthReport report) {
        return new HealthReportResponse(
                report.vin().value(),
                report.assessment().score(),
                report.assessment().status(),
                report.id(),
                report.telemetryId(),
                report.telemetryRecordedAt(),
                report.analyzedAt(),
                report.assessment().alerts().stream().map(AlertResponse::from).toList());
    }
}
