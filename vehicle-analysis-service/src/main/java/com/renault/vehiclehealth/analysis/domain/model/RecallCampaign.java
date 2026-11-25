package com.renault.vehiclehealth.analysis.domain.model;

import java.util.Objects;

public record RecallCampaign(String code, String description) {

    public RecallCampaign {
        Objects.requireNonNull(code, "code is required");
    }
}
