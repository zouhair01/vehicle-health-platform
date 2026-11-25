package com.renault.vehiclehealth.analysis.domain.port;

import com.renault.vehiclehealth.analysis.domain.model.RecallCampaign;
import com.renault.vehiclehealth.analysis.domain.model.Vin;

import java.util.List;

public interface RecallCampaignProvider {

    /**
     * @return the open recall campaigns for the vehicle, empty if the vehicle is unknown
     * @throws RecallProviderUnavailableException if the provider cannot be reached or fails
     */
    List<RecallCampaign> findOpenCampaigns(Vin vin);
}
