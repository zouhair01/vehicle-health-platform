package com.renault.vehiclehealth.telemetry.infrastructure.persistence;

import com.renault.vehiclehealth.telemetry.domain.model.Telemetry;
import com.renault.vehiclehealth.telemetry.domain.port.TelemetryRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTelemetryRepository implements TelemetryRepository {

    private final Map<UUID, Telemetry> store = new ConcurrentHashMap<>();

    @Override
    public void save(Telemetry telemetry) {
        store.put(telemetry.id(), telemetry);
    }
}
