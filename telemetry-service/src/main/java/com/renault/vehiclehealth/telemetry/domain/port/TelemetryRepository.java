package com.renault.vehiclehealth.telemetry.domain.port;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;

public interface TelemetryRepository {

    void save(Telemetry telemetry);
}
