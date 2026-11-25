package com.renault.vehiclehealth.telemetry.infrastructure.messaging;

import com.jayway.jsonpath.JsonPath;
import com.renault.vehiclehealth.telemetry.TestcontainersConfiguration;
import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.model.TelemetryReading;
import com.renault.vehiclehealth.telemetry.domain.model.TirePressure;
import com.renault.vehiclehealth.telemetry.domain.model.Vin;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@Import(TestcontainersConfiguration.class)
class KafkaTelemetryEventPublisherTest {

    @Autowired
    private KafkaTelemetryEventPublisher publisher;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Value("${app.kafka.topics.telemetry-received}")
    private String topic;

    @Test
    void publishesEventKeyedByVinWithTelemetryPayload() {
        UUID telemetryId = UUID.randomUUID();
        Telemetry telemetry = new Telemetry(telemetryId, new TelemetryReading(
                new Vin("VF1RFB00123456789"), Instant.parse("2026-11-25T10:00:00Z"), 85_000,
                new TirePressure(2.3, 2.3, 2.4, 2.4), 12.6, 90, 45, List.of("P0300")),
                Instant.parse("2026-11-25T10:05:00Z"));

        try (Consumer<String, String> consumer =
                     consumerFactory.createConsumer("test-" + UUID.randomUUID(), null)) {
            consumer.subscribe(List.of(topic));

            publisher.publishTelemetryReceived(telemetry);

            ConsumerRecord<String, String> record =
                    KafkaTestUtils.getSingleRecord(consumer, topic, Duration.ofSeconds(10));
            String json = record.value();

            assertThat(record.key()).isEqualTo("VF1RFB00123456789");
            assertThat((String) JsonPath.read(json, "$.eventType")).isEqualTo("TelemetryReceived");
            assertThat((String) JsonPath.read(json, "$.telemetryId")).isEqualTo(telemetryId.toString());
            assertThat((String) JsonPath.read(json, "$.occurredAt")).isEqualTo("2026-11-25T10:05:00Z");
            assertThat((Integer) JsonPath.read(json, "$.mileageKm")).isEqualTo(85_000);
            assertThat((List<String>) JsonPath.read(json, "$.errorCodes")).containsExactly("P0300");
        }
    }
}
