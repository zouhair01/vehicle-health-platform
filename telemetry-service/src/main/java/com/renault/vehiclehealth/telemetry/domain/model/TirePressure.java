package com.renault.vehiclehealth.telemetry.domain.model;

public record TirePressure(double frontLeftBar, double frontRightBar, double rearLeftBar, double rearRightBar) {

    private static final double MAX_BAR = 10.0;

    public TirePressure {
        requireInRange("frontLeftBar", frontLeftBar);
        requireInRange("frontRightBar", frontRightBar);
        requireInRange("rearLeftBar", rearLeftBar);
        requireInRange("rearRightBar", rearRightBar);
    }

    private static void requireInRange(String name, double value) {
        if (value < 0 || value > MAX_BAR) {
            throw new InvalidTelemetryException(name + " must be between 0 and " + MAX_BAR + " bar");
        }
    }
}
