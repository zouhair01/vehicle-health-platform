package com.renault.vehiclehealth.telemetry.infrastructure.web;

import com.renault.vehiclehealth.telemetry.application.ReceiveTelemetryUseCase;
import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/telemetry")
@Tag(name = "Telemetry", description = "Vehicle telemetry ingestion")
public class TelemetryController {

    private final ReceiveTelemetryUseCase receiveTelemetry;

    public TelemetryController(ReceiveTelemetryUseCase receiveTelemetry) {
        this.receiveTelemetry = receiveTelemetry;
    }

    @PostMapping
    @Operation(summary = "Submit vehicle telemetry",
            description = "Stores the telemetry and publishes a TelemetryReceived event; the health analysis runs asynchronously.")
    @ApiResponse(responseCode = "202", description = "Telemetry accepted")
    @ApiResponse(responseCode = "400", description = "Invalid payload (missing field, invalid VIN, value out of range, malformed JSON)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected error",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<TelemetryAcceptedResponse> receive(@Valid @RequestBody TelemetryRequest request) {
        Telemetry telemetry = receiveTelemetry.receive(request.toReading());
        return ResponseEntity.accepted().body(TelemetryAcceptedResponse.from(telemetry));
    }
}
