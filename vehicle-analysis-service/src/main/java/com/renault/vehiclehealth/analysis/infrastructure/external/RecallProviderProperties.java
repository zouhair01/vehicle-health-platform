package com.renault.vehiclehealth.analysis.infrastructure.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.recall-provider")
public record RecallProviderProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
