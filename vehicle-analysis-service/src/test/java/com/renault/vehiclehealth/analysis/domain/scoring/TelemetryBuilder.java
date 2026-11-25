package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.RecallCampaign;
import com.renault.vehiclehealth.analysis.domain.model.TirePressure;
import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;
import com.renault.vehiclehealth.analysis.domain.model.VehicleTelemetry;
import com.renault.vehiclehealth.analysis.domain.model.Vin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

final class TelemetryBuilder {

    private long mileageKm = 50_000;
    private TirePressure tirePressure = new TirePressure(2.4, 2.4, 2.4, 2.4);
    private double batteryVoltage = 12.6;
    private double engineTemperatureCelsius = 90;
    private double fuelLevelPercent = 60;
    private List<String> errorCodes = List.of();
    private List<RecallCampaign> recallCampaigns = List.of();

    static TelemetryBuilder aHealthyVehicle() {
        return new TelemetryBuilder();
    }

    TelemetryBuilder mileageKm(long value) { this.mileageKm = value; return this; }
    TelemetryBuilder tirePressure(double fl, double fr, double rl, double rr) { this.tirePressure = new TirePressure(fl, fr, rl, rr); return this; }
    TelemetryBuilder batteryVoltage(double value) { this.batteryVoltage = value; return this; }
    TelemetryBuilder engineTemperature(double value) { this.engineTemperatureCelsius = value; return this; }
    TelemetryBuilder fuelLevel(double value) { this.fuelLevelPercent = value; return this; }
    TelemetryBuilder errorCodes(String... codes) { this.errorCodes = List.of(codes); return this; }
    TelemetryBuilder recallCampaigns(RecallCampaign... campaigns) { this.recallCampaigns = List.of(campaigns); return this; }

    VehicleCondition build() {
        VehicleTelemetry telemetry = new VehicleTelemetry(
                UUID.randomUUID(), new Vin("VF1RFB00123456789"), Instant.parse("2026-11-25T10:00:00Z"),
                mileageKm, tirePressure, batteryVoltage, engineTemperatureCelsius, fuelLevelPercent, errorCodes);
        return new VehicleCondition(telemetry, recallCampaigns);
    }
}
