package com.renault.vehiclehealth.analysis.application;

import com.renault.vehiclehealth.analysis.domain.model.Vin;

public class HealthReportNotFoundException extends RuntimeException {

    public HealthReportNotFoundException(Vin vin) {
        super("No health report found for VIN " + vin);
    }
}
