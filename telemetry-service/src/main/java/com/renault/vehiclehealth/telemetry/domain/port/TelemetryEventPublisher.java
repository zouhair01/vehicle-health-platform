package com.renault.vehiclehealth.telemetry.domain.port;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;

public interface TelemetryEventPublisher {

    void publishTelemetryReceived(Telemetry telemetry);
}
