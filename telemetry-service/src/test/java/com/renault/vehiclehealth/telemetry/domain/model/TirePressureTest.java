package com.renault.vehiclehealth.telemetry.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TirePressureTest {

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 10.1})
    void rejectsPressureOutOfRange(double pressure) {
        assertThatThrownBy(() -> new TirePressure(pressure, 2.3, 2.3, 2.3))
                .isInstanceOf(InvalidTelemetryException.class)
                .hasMessageContaining("frontLeftBar");
    }
}
