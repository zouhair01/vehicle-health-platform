package com.renault.vehiclehealth.analysis.acceptance;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.renault.vehiclehealth.analysis.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class VehicleHealthAcceptanceTest {

    private static final String RECALLS_PATH = "/api/v1/recalls";
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @RegisterExtension
    static WireMockExtension recallProvider = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void recallProviderUrl(DynamicPropertyRegistry registry) {
        registry.add("app.recall-provider.base-url", recallProvider::baseUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JdbcClient jdbcClient;

    @Value("${app.kafka.topics.telemetry-received}")
    private String topic;

    @BeforeEach
    void setUp() {
        jdbcClient.sql("DELETE FROM health_report").update();
        recallProvider.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo(RECALLS_PATH))
                .willReturn(okJson("{\"campaigns\": []}")));
    }

    @Test
    @DisplayName("Scenario 2 - given telemetry data, when analysis is done, then the score is computed correctly")
    void scenario2_healthScoreIsComputedCorrectly() {
        String vin = "VF1RFB00000000002";
        publish(event(vin, 160_000, 2.4, 12.0, 90, "[\"P0300\"]"));

        // 100 - 10 (mileage > 150,000 km) - 10 (battery < 12.2 V) - 10 (1 error code)
        await().atMost(TIMEOUT).untilAsserted(() ->
                mockMvc.perform(get("/api/v1/vehicles/{vin}/health-report", vin))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.score").value(70))
                        .andExpect(jsonPath("$.status").value("ATTENTION_REQUIRED"))
                        .andExpect(jsonPath("$.alerts.length()").value(3)));
    }

    @Test
    @DisplayName("Scenario 3 - given WireMock configured, when the external API is called, then the simulated response is used")
    void scenario3_simulatedRecallProviderResponseIsUsed() {
        String vin = "VF1RFB00000000003";
        recallProvider.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo(RECALLS_PATH))
                .withQueryParam("vin", equalTo(vin))
                .willReturn(okJson("""
                        { "vin": "%s", "campaigns": [
                            { "code": "RC-2026-014", "description": "Brake hose inspection", "status": "OPEN" },
                            { "code": "RC-2026-020", "description": "Airbag sensor replacement", "status": "OPEN" },
                            { "code": "RC-2025-101", "description": "Software update", "status": "CLOSED" } ] }
                        """.formatted(vin))));

        publish(event(vin, 50_000, 2.4, 12.6, 90, "[]"));

        // 100 - 2 x 15 (two open recall campaigns, the closed one is ignored)
        await().atMost(TIMEOUT).untilAsserted(() ->
                mockMvc.perform(get("/api/v1/vehicles/{vin}/health-report", vin))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.score").value(70))
                        .andExpect(jsonPath("$.alerts[*].type").value(
                                containsInAnyOrder("RECALL_CAMPAIGN", "RECALL_CAMPAIGN"))));
        recallProvider.verify(getRequestedFor(urlPathEqualTo(RECALLS_PATH)).withQueryParam("vin", equalTo(vin)));
    }

    @Test
    @DisplayName("Scenario 4 - given detected anomalies, when alerts are retrieved, then alerts are returned correctly")
    void scenario4_alertsAreReturnedCorrectly() throws Exception {
        String vin = "VF1RFB00000000004";
        publish(event(vin, 50_000, 1.2, 12.6, 120, "[\"P0171\"]"));

        // 4 critical tire alerts + 1 critical engine temperature alert + 1 warning error code alert
        await().atMost(TIMEOUT).untilAsserted(() ->
                mockMvc.perform(get("/api/v1/vehicles/{vin}/alerts", vin))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.vin").value(vin))
                        .andExpect(jsonPath("$.count").value(6)));

        mockMvc.perform(get("/api/v1/vehicles/{vin}/alerts", vin).param("severity", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
        mockMvc.perform(get("/api/v1/vehicles/{vin}/alerts", vin).param("severity", "WARNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.alerts[0].type").value("ERROR_CODE"));
    }

    private void publish(String event) {
        kafkaTemplate.send(topic, event.substring(event.indexOf("VF1"), event.indexOf("VF1") + 17), event).join();
    }

    private static String event(String vin, long mileageKm, double tireBar, double batteryVoltage,
                                double engineTemperature, String errorCodesJson) {
        return """
                {
                  "eventType": "TelemetryReceived",
                  "occurredAt": "2026-11-25T10:05:00Z",
                  "telemetryId": "%s",
                  "vin": "%s",
                  "recordedAt": "2026-11-25T10:00:00Z",
                  "mileageKm": %d,
                  "tirePressure": { "frontLeftBar": %s, "frontRightBar": %s, "rearLeftBar": %s, "rearRightBar": %s },
                  "batteryVoltage": %s,
                  "engineTemperatureCelsius": %s,
                  "fuelLevelPercent": 60,
                  "errorCodes": %s
                }
                """.formatted(UUID.randomUUID(), vin, mileageKm, tireBar, tireBar, tireBar, tireBar,
                batteryVoltage, engineTemperature, errorCodesJson);
    }
}
