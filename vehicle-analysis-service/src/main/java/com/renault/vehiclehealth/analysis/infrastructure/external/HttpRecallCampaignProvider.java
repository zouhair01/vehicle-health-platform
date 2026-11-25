package com.renault.vehiclehealth.analysis.infrastructure.external;

import com.renault.vehiclehealth.analysis.domain.model.RecallCampaign;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import com.renault.vehiclehealth.analysis.domain.port.RecallCampaignProvider;
import com.renault.vehiclehealth.analysis.domain.port.RecallProviderUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class HttpRecallCampaignProvider implements RecallCampaignProvider {

    private final RestClient restClient;

    public HttpRecallCampaignProvider(RestClient recallProviderRestClient) {
        this.restClient = recallProviderRestClient;
    }

    @Override
    public List<RecallCampaign> findOpenCampaigns(Vin vin) {
        RecallProviderResponse response;
        try {
            response = restClient.get()
                    .uri("/api/v1/recalls?vin={vin}", vin.value())
                    .retrieve()
                    .body(RecallProviderResponse.class);
        } catch (HttpClientErrorException.NotFound exception) {
            return List.of();
        } catch (RestClientException exception) {
            throw new RecallProviderUnavailableException("Recall provider call failed for VIN " + vin, exception);
        }

        if (response == null || response.campaigns() == null) {
            return List.of();
        }
        return response.campaigns().stream()
                .filter(RecallProviderResponse.Campaign::isOpen)
                .map(campaign -> new RecallCampaign(campaign.code(), campaign.description()))
                .toList();
    }
}
