package com.renault.vehiclehealth.telemetry.infrastructure.persistence;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.model.TelemetryReading;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "telemetry")
public class TelemetryEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false, length = 17)
    private String vin;

    @Column(nullable = false)
    private Instant recordedAt;

    @Column(nullable = false)
    private Instant receivedAt;

    private long mileageKm;
    private double tireFrontLeftBar;
    private double tireFrontRightBar;
    private double tireRearLeftBar;
    private double tireRearRightBar;
    private double batteryVoltage;
    private double engineTemperatureCelsius;
    private double fuelLevelPercent;

    @ElementCollection
    @CollectionTable(name = "telemetry_error_code", joinColumns = @JoinColumn(name = "telemetry_id"))
    @Column(name = "code", nullable = false, length = 5)
    private List<String> errorCodes = new ArrayList<>();

    @Transient
    private boolean isNew = true;

    protected TelemetryEntity() {
    }

    static TelemetryEntity from(Telemetry telemetry) {
        TelemetryReading reading = telemetry.reading();
        TelemetryEntity entity = new TelemetryEntity();
        entity.id = telemetry.id();
        entity.vin = reading.vin().value();
        entity.recordedAt = reading.recordedAt();
        entity.receivedAt = telemetry.receivedAt();
        entity.mileageKm = reading.mileageKm();
        entity.tireFrontLeftBar = reading.tirePressure().frontLeftBar();
        entity.tireFrontRightBar = reading.tirePressure().frontRightBar();
        entity.tireRearLeftBar = reading.tirePressure().rearLeftBar();
        entity.tireRearRightBar = reading.tirePressure().rearRightBar();
        entity.batteryVoltage = reading.batteryVoltage();
        entity.engineTemperatureCelsius = reading.engineTemperatureCelsius();
        entity.fuelLevelPercent = reading.fuelLevelPercent();
        entity.errorCodes = new ArrayList<>(reading.errorCodes());
        return entity;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        isNew = false;
    }
}
