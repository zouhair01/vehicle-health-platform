package com.renault.vehiclehealth.telemetry.infrastructure.web;

import com.renault.vehiclehealth.telemetry.application.ReceiveTelemetryUseCase;
import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.model.TelemetryReading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TelemetryController.class)
class TelemetryControllerTest {

    private static final String VALID_PAYLOAD = """
            {
              "vin": "VF1RFB00123456789",
              "recordedAt": "2026-11-25T10:00:00Z",
              "mileageKm": 85000,
              "tirePressure": {
                "frontLeftBar": 2.3, "frontRightBar": 2.3, "rearLeftBar": 2.4, "rearRightBar": 2.4
              },
              "batteryVoltage": 12.6,
              "engineTemperatureCelsius": 90,
              "fuelLevelPercent": 45,
              "errorCodes": ["P0300"]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReceiveTelemetryUseCase receiveTelemetry;

    @Test
    void returnsAcceptedWhenPayloadIsValid() throws Exception {
        UUID telemetryId = UUID.fromString("3f2b8c1e-5d4a-4b7e-9c0f-1a2b3c4d5e6f");
        Instant receivedAt = Instant.parse("2026-11-25T10:05:00Z");
        when(receiveTelemetry.receive(any())).thenAnswer(invocation ->
                new Telemetry(telemetryId, invocation.<TelemetryReading>getArgument(0), receivedAt));

        mockMvc.perform(post("/api/v1/telemetry").contentType(MediaType.APPLICATION_JSON).content(VALID_PAYLOAD))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.telemetryId").value(telemetryId.toString()))
                .andExpect(jsonPath("$.vin").value("VF1RFB00123456789"))
                .andExpect(jsonPath("$.receivedAt").value("2026-11-25T10:05:00Z"));
    }

    @Test
    void returnsBadRequestWithFieldErrorsWhenRequiredFieldIsMissing() throws Exception {
        String payload = VALID_PAYLOAD.replace("\"vin\": \"VF1RFB00123456789\",", "");

        mockMvc.perform(post("/api/v1/telemetry").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.vin").exists());

        verifyNoInteractions(receiveTelemetry);
    }

    @Test
    void returnsBadRequestWhenRecordedAtIsInTheFuture() throws Exception {
        String payload = VALID_PAYLOAD.replace("2026-11-25T10:00:00Z", "2999-01-01T00:00:00Z");

        mockMvc.perform(post("/api/v1/telemetry").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.recordedAt").exists());
    }

    @Test
    void returnsBadRequestWhenVinFormatIsInvalid() throws Exception {
        String payload = VALID_PAYLOAD.replace("VF1RFB00123456789", "INVALID-VIN");

        mockMvc.perform(post("/api/v1/telemetry").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid telemetry"));

        verifyNoInteractions(receiveTelemetry);
    }

    @Test
    void returnsBadRequestWhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/telemetry").contentType(MediaType.APPLICATION_JSON).content("{ not json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(receiveTelemetry);
    }
}
