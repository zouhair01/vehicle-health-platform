package com.renault.vehiclehealth.telemetry.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI telemetryServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Telemetry Service API")
                .version("1.0.0")
                .description("Collects vehicle telemetry and triggers an asynchronous health analysis"));
    }
}
