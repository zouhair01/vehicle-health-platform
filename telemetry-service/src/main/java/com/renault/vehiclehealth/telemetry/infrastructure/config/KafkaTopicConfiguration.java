package com.renault.vehiclehealth.telemetry.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    NewTopic telemetryReceivedTopic(@Value("${app.kafka.topics.telemetry-received}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
