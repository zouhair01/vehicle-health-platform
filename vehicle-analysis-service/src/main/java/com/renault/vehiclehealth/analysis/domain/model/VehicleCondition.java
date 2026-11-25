package com.renault.vehiclehealth.analysis.domain.model;

import java.util.List;
import java.util.Objects;

public record VehicleCondition(VehicleTelemetry telemetry, List<RecallCampaign> openRecallCampaigns) {

    public VehicleCondition {
        Objects.requireNonNull(telemetry, "telemetry is required");
        openRecallCampaigns = openRecallCampaigns == null ? List.of() : List.copyOf(openRecallCampaigns);
    }
}
