package com.renault.vehiclehealth.analysis.infrastructure.config;

import com.renault.vehiclehealth.analysis.infrastructure.messaging.InvalidTelemetryMessageException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfiguration {

    private static final int PARTITIONS = 3;
    private static final long RETRY_INTERVAL_MS = 1_000;
    private static final long MAX_RETRIES = 3;

    @Bean
    NewTopic telemetryReceivedTopic(@Value("${app.kafka.topics.telemetry-received}") String topic) {
        return TopicBuilder.name(topic).partitions(PARTITIONS).replicas(1).build();
    }

    @Bean
    NewTopic telemetryReceivedDeadLetterTopic(@Value("${app.kafka.topics.telemetry-received-dlt}") String topic) {
        return TopicBuilder.name(topic).partitions(PARTITIONS).replicas(1).build();
    }

    @Bean
    CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate,
                                         @Value("${app.kafka.topics.telemetry-received-dlt}") String deadLetterTopic) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new TopicPartition(deadLetterTopic, record.partition()));
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES));
        errorHandler.addNotRetryableExceptions(InvalidTelemetryMessageException.class);
        return errorHandler;
    }
}
