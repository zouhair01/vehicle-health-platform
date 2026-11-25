package com.renault.vehiclehealth.telemetry.acceptance;

import com.renault.vehiclehealth.telemetry.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TelemetryIngestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void cleanDatabase() {
        jdbcClient.sql("DELETE FROM telemetry").update();
    }

    @Test
    void scenario1_validPayloadIsAcceptedAndStored() throws Exception {
        String payload = """
                {
                  "vin": "VF1RFB00123456789",
                  "recordedAt": "2026-11-25T10:00:00Z",
                  "mileageKm": 85000,
                  "tirePressure": {
                    "frontLeftBar": 2.3, "frontRightBar": 2.3, "rearLeftBar": 2.4, "rearRightBar": 2.4
                  },
                  "batteryVoltage": 12.6,
                  "engineTemperatureCelsius": 90,
                  "fuelLevelPercent": 45,
                  "errorCodes": ["P0300"]
                }
                """;

        mockMvc.perform(post("/api/v1/telemetry").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.telemetryId").exists());

        Long stored = jdbcClient.sql("SELECT COUNT(*) FROM telemetry WHERE vin = ?")
                .param("VF1RFB00123456789").query(Long.class).single();
        assertThat(stored).isEqualTo(1L);
    }
}
