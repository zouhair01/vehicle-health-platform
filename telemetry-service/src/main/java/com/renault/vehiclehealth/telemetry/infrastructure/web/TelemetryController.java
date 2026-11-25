package com.renault.vehiclehealth.telemetry.infrastructure.web;

import com.renault.vehiclehealth.telemetry.application.ReceiveTelemetryUseCase;
import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final ReceiveTelemetryUseCase receiveTelemetry;

    public TelemetryController(ReceiveTelemetryUseCase receiveTelemetry) {
        this.receiveTelemetry = receiveTelemetry;
    }

    @PostMapping
    public ResponseEntity<TelemetryAcceptedResponse> receive(@Valid @RequestBody TelemetryRequest request) {
        Telemetry telemetry = receiveTelemetry.receive(request.toReading());
        return ResponseEntity.accepted().body(TelemetryAcceptedResponse.from(telemetry));
    }
}
