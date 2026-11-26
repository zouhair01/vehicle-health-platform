package com.renault.vehiclehealth.analysis.infrastructure.messaging;

import com.renault.vehiclehealth.analysis.application.AnalyzeVehicleUseCase;
import com.renault.vehiclehealth.analysis.domain.model.VehicleTelemetry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class TelemetryReceivedListener {

    private static final Logger log = LoggerFactory.getLogger(TelemetryReceivedListener.class);

    private final AnalyzeVehicleUseCase analyzeVehicle;
    private final JsonMapper jsonMapper;

    public TelemetryReceivedListener(AnalyzeVehicleUseCase analyzeVehicle, JsonMapper jsonMapper) {
        this.analyzeVehicle = analyzeVehicle;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.telemetry-received}")
    public void onTelemetryReceived(ConsumerRecord<String, String> record) {
        VehicleTelemetry telemetry = parse(record.value());
        analyzeVehicle.analyze(telemetry).ifPresentOrElse(
                report -> log.info("Health report {} computed for VIN {}: score {} ({})",
                        report.id(), report.vin(), report.assessment().score(), report.assessment().status()),
                () -> log.info("Telemetry {} already analyzed, message skipped", telemetry.telemetryId()));
    }

    private VehicleTelemetry parse(String payload) {
        try {
            return jsonMapper.readValue(payload, TelemetryReceivedMessage.class).toDomain();
        } catch (RuntimeException exception) {
            throw new InvalidTelemetryMessageException("Invalid telemetry message", exception);
        }
    }
}
