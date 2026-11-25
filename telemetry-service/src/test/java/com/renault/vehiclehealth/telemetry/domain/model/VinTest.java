package com.renault.vehiclehealth.telemetry.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VinTest {

    @Test
    void acceptsValidVin() {
        assertThat(new Vin("VF1RFB00123456789").value()).isEqualTo("VF1RFB00123456789");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "VF1RFB0012345678", "VF1RFB001234567890", "VF1RFB0012345678O", "vf1rfb00123456789"})
    void rejectsInvalidVin(String value) {
        assertThatThrownBy(() -> new Vin(value)).isInstanceOf(InvalidTelemetryException.class);
    }
}
