package com.renault.vehiclehealth.telemetry.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelemetryReadingTest {

    private static final Vin VIN = new Vin("VF1RFB00123456789");
    private static final Instant RECORDED_AT = Instant.parse("2026-11-25T10:00:00Z");
    private static final TirePressure TIRES = new TirePressure(2.3, 2.3, 2.4, 2.4);

    @Test
    void createsReadingWithValidValues() {
        TelemetryReading reading = reading(85_000, 45, List.of("P0300"));

        assertThat(reading.vin()).isEqualTo(VIN);
        assertThat(reading.mileageKm()).isEqualTo(85_000);
        assertThat(reading.errorCodes()).containsExactly("P0300");
    }

    @Test
    void defaultsMissingErrorCodesToEmptyList() {
        assertThat(reading(85_000, 45, null).errorCodes()).isEmpty();
    }

    @Test
    void keepsErrorCodesImmutable() {
        List<String> codes = new ArrayList<>(List.of("P0300"));
        TelemetryReading reading = reading(85_000, 45, codes);
        codes.add("P0171");

        assertThat(reading.errorCodes()).containsExactly("P0300");
        assertThatThrownBy(() -> reading.errorCodes().add("P0171"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsNegativeMileage() {
        assertThatThrownBy(() -> reading(-1, 45, List.of()))
                .isInstanceOf(InvalidTelemetryException.class)
                .hasMessageContaining("mileageKm");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 100.1})
    void rejectsFuelLevelOutOfRange(double fuelLevel) {
        assertThatThrownBy(() -> reading(85_000, fuelLevel, List.of()))
                .isInstanceOf(InvalidTelemetryException.class)
                .hasMessageContaining("fuelLevelPercent");
    }

    @ParameterizedTest
    @ValueSource(strings = {"X0300", "P03", "p0300"})
    void rejectsMalformedErrorCode(String code) {
        assertThatThrownBy(() -> reading(85_000, 45, List.of(code)))
                .isInstanceOf(InvalidTelemetryException.class)
                .hasMessageContaining("invalid error code");
    }

    private static TelemetryReading reading(long mileageKm, double fuelLevel, List<String> errorCodes) {
        return new TelemetryReading(VIN, RECORDED_AT, mileageKm, TIRES, 12.6, 90, fuelLevel, errorCodes);
    }
}
