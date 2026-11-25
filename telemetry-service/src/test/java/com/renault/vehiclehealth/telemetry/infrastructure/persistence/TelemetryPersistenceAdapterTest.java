package com.renault.vehiclehealth.telemetry.infrastructure.persistence;

import com.renault.vehiclehealth.telemetry.TestcontainersConfiguration;
import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.model.TelemetryReading;
import com.renault.vehiclehealth.telemetry.domain.model.TirePressure;
import com.renault.vehiclehealth.telemetry.domain.model.Vin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TelemetryPersistenceAdapterTest {

    @Autowired
    private TelemetryPersistenceAdapter adapter;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void cleanDatabase() {
        jdbcClient.sql("DELETE FROM telemetry").update();
    }

    @Test
    void savesTelemetryWithItsErrorCodes() {
        UUID id = UUID.randomUUID();
        Telemetry telemetry = new Telemetry(id, new TelemetryReading(
                new Vin("VF1RFB00123456789"), Instant.parse("2026-11-25T10:00:00Z"), 85_000,
                new TirePressure(2.3, 2.3, 2.4, 2.4), 12.6, 90, 45, List.of("P0300", "P0171")),
                Instant.parse("2026-11-25T10:05:00Z"));

        adapter.save(telemetry);

        String vin = jdbcClient.sql("SELECT vin FROM telemetry WHERE id = ?")
                .param(id).query(String.class).single();
        Long mileage = jdbcClient.sql("SELECT mileage_km FROM telemetry WHERE id = ?")
                .param(id).query(Long.class).single();
        List<String> codes = jdbcClient.sql("SELECT code FROM telemetry_error_code WHERE telemetry_id = ? ORDER BY code")
                .param(id).query(String.class).list();

        assertThat(vin).isEqualTo("VF1RFB00123456789");
        assertThat(mileage).isEqualTo(85_000L);
        assertThat(codes).containsExactly("P0171", "P0300");
    }
}
