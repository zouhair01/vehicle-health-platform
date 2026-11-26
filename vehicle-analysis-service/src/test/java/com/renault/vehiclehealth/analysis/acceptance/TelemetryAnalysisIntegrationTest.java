package com.renault.vehiclehealth.analysis.acceptance;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.renault.vehiclehealth.analysis.TestcontainersConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TelemetryAnalysisIntegrationTest {

    private static final String VIN = "VF1RFB00123456789";
    private static final String RECALLS_PATH = "/api/v1/recalls";

    @RegisterExtension
    static WireMockExtension recallProvider = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void recallProviderUrl(DynamicPropertyRegistry registry) {
        registry.add("app.recall-provider.base-url", recallProvider::baseUrl);
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private JdbcClient jdbcClient;

    @Value("${app.kafka.topics.telemetry-received}")
    private String topic;

    @Value("${app.kafka.topics.telemetry-received-dlt}")
    private String deadLetterTopic;

    @BeforeEach
    void setUp() {
        jdbcClient.sql("DELETE FROM health_report").update();
        recallProvider.stubFor(get(urlPathEqualTo(RECALLS_PATH)).willReturn(okJson("{\"campaigns\": []}")));
    }

    @Test
    void scenario2And3_analyzesTelemetryEventUsingSimulatedRecallProvider() {
        recallProvider.stubFor(get(urlPathEqualTo(RECALLS_PATH))
                .withQueryParam("vin", equalTo(VIN))
                .willReturn(okJson("""
                        { "vin": "VF1RFB00123456789",
                          "campaigns": [ { "code": "RC-2026-014", "description": "Brake hose inspection", "status": "OPEN" } ] }
                        """)));
        UUID telemetryId = UUID.randomUUID();

        kafkaTemplate.send(topic, VIN, telemetryEvent(telemetryId)).join();

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(scoreOf(telemetryId)).isEqualTo(75));
        String status = jdbcClient.sql("SELECT status FROM health_report WHERE telemetry_id = ?")
                .param(telemetryId).query(String.class).single();
        List<String> alertTypes = jdbcClient.sql("""
                        SELECT a.type FROM health_alert a JOIN health_report r ON r.id = a.report_id
                        WHERE r.telemetry_id = ? ORDER BY a.alert_index""")
                .param(telemetryId).query(String.class).list();

        assertThat(status).isEqualTo("ATTENTION_REQUIRED");
        assertThat(alertTypes).containsExactly("ERROR_CODE", "RECALL_CAMPAIGN");
        recallProvider.verify(getRequestedFor(urlPathEqualTo(RECALLS_PATH)).withQueryParam("vin", equalTo(VIN)));
    }

    @Test
    void sameTelemetryEventIsAnalyzedOnlyOnce() {
        UUID telemetryId = UUID.randomUUID();
        String event = telemetryEvent(telemetryId);

        kafkaTemplate.send(topic, VIN, event).join();
        kafkaTemplate.send(topic, VIN, event).join();

        await().atMost(Duration.ofSeconds(20)).during(Duration.ofSeconds(3)).untilAsserted(() ->
                assertThat(reportCount(telemetryId)).isEqualTo(1L));
    }

    @Test
    void malformedMessageIsSentToDeadLetterTopic() {
        try (Consumer<String, String> consumer = consumerFactory.createConsumer("dlt-test-" + UUID.randomUUID(), null)) {
            consumer.subscribe(List.of(deadLetterTopic));

            kafkaTemplate.send(topic, VIN, "{ not json").join();

            ConsumerRecord<String, String> record =
                    KafkaTestUtils.getSingleRecord(consumer, deadLetterTopic, Duration.ofSeconds(20));
            assertThat(record.value()).isEqualTo("{ not json");
        }
    }

    private Integer scoreOf(UUID telemetryId) {
        return jdbcClient.sql("SELECT score FROM health_report WHERE telemetry_id = ?")
                .param(telemetryId).query(Integer.class).optional().orElse(null);
    }

    private Long reportCount(UUID telemetryId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM health_report WHERE telemetry_id = ?")
                .param(telemetryId).query(Long.class).single();
    }

    private static String telemetryEvent(UUID telemetryId) {
        return """
                {
                  "eventType": "TelemetryReceived",
                  "occurredAt": "2026-11-25T10:05:00Z",
                  "telemetryId": "%s",
                  "vin": "VF1RFB00123456789",
                  "recordedAt": "2026-11-25T10:00:00Z",
                  "mileageKm": 85000,
                  "tirePressure": { "frontLeftBar": 2.3, "frontRightBar": 2.3, "rearLeftBar": 2.4, "rearRightBar": 2.4 },
                  "batteryVoltage": 12.6,
                  "engineTemperatureCelsius": 90,
                  "fuelLevelPercent": 45,
                  "errorCodes": ["P0300"]
                }
                """.formatted(telemetryId);
    }
}
