package com.renault.vehiclehealth.telemetry.infrastructure.messaging;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.port.TelemetryEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaTelemetryEventPublisher implements TelemetryEventPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final String topic;

    public KafkaTelemetryEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                        JsonMapper jsonMapper,
                                        @Value("${app.kafka.topics.telemetry-received}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.topic = topic;
    }

    @Override
    public void publishTelemetryReceived(Telemetry telemetry) {
        String key = telemetry.reading().vin().value();
        String payload = jsonMapper.writeValueAsString(TelemetryReceivedEvent.from(telemetry));
        try {
            kafkaTemplate.send(topic, key, payload).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new EventPublicationException("Interrupted while publishing telemetry " + telemetry.id(), exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new EventPublicationException("Failed to publish telemetry " + telemetry.id(), exception);
        }
    }
}
