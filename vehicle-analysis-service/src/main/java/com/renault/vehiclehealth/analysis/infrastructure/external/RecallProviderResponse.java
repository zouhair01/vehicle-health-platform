package com.renault.vehiclehealth.analysis.infrastructure.external;

import java.util.List;

record RecallProviderResponse(String vin, List<Campaign> campaigns) {

    record Campaign(String code, String description, String status) {

        boolean isOpen() {
            return "OPEN".equalsIgnoreCase(status);
        }
    }
}
