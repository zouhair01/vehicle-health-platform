package com.renault.vehiclehealth.analysis.domain.model;

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
    @ValueSource(strings = {"", "SHORT", "VF1RFB0012345678O"})
    void rejectsInvalidVin(String value) {
        assertThatThrownBy(() -> new Vin(value)).isInstanceOf(InvalidVinException.class);
    }
}
