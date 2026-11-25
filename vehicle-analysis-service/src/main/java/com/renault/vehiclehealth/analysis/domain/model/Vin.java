package com.renault.vehiclehealth.analysis.domain.model;

import java.util.regex.Pattern;

public record Vin(String value) {

    private static final Pattern VIN_FORMAT = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    public Vin {
        if (value == null || !VIN_FORMAT.matcher(value).matches()) {
            throw new InvalidVinException(
                    "VIN must contain 17 uppercase alphanumeric characters, excluding I, O and Q");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
