package com.renault.vehiclehealth.analysis.domain.scoring;

import com.renault.vehiclehealth.analysis.domain.model.VehicleCondition;

public interface HealthRule {

    RuleOutcome evaluate(VehicleCondition condition);
}
