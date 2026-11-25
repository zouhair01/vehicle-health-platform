package com.renault.vehiclehealth.analysis.infrastructure.external;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.renault.vehiclehealth.analysis.domain.model.RecallCampaign;
import com.renault.vehiclehealth.analysis.domain.model.Vin;
import com.renault.vehiclehealth.analysis.domain.port.RecallProviderUnavailableException;
import com.renault.vehiclehealth.analysis.infrastructure.config.RecallProviderConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpRecallCampaignProviderTest {

    private static final Vin VIN = new Vin("VF1RFB00123456789");
    private static final String RECALLS_PATH = "/api/v1/recalls";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private HttpRecallCampaignProvider provider;

    @BeforeEach
    void setUp() {
        RecallProviderProperties properties =
                new RecallProviderProperties(wireMock.baseUrl(), Duration.ofSeconds(1), Duration.ofMillis(500));
        RestClient restClient = new RecallProviderConfiguration()
                .recallProviderRestClient(RestClient.builder(), properties);
        provider = new HttpRecallCampaignProvider(restClient);
    }

    @Test
    void returnsOnlyOpenCampaigns() {
        wireMock.stubFor(get(urlPathEqualTo(RECALLS_PATH))
                .withQueryParam("vin", equalTo(VIN.value()))
                .willReturn(okJson("""
                        {
                          "vin": "VF1RFB00123456789",
                          "campaigns": [
                            { "code": "RC-2026-014", "description": "Brake hose inspection", "status": "OPEN" },
                            { "code": "RC-2025-101", "description": "Software update", "status": "CLOSED" }
                          ]
                        }
                        """)));

        List<RecallCampaign> campaigns = provider.findOpenCampaigns(VIN);

        assertThat(campaigns).containsExactly(new RecallCampaign("RC-2026-014", "Brake hose inspection"));
        wireMock.verify(getRequestedFor(urlPathEqualTo(RECALLS_PATH)).withQueryParam("vin", equalTo(VIN.value())));
    }

    @Test
    void returnsNoCampaignWhenVehicleIsUnknown() {
        wireMock.stubFor(get(urlPathEqualTo(RECALLS_PATH)).willReturn(aResponse().withStatus(404)));

        assertThat(provider.findOpenCampaigns(VIN)).isEmpty();
    }

    @Test
    void failsWhenProviderReturnsServerError() {
        wireMock.stubFor(get(urlPathEqualTo(RECALLS_PATH)).willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> provider.findOpenCampaigns(VIN))
                .isInstanceOf(RecallProviderUnavailableException.class)
                .hasMessageContaining(VIN.value());
    }

    @Test
    void failsWhenProviderIsTooSlow() {
        wireMock.stubFor(get(urlPathEqualTo(RECALLS_PATH))
                .willReturn(okJson("{\"campaigns\": []}").withFixedDelay(2_000)));

        assertThatThrownBy(() -> provider.findOpenCampaigns(VIN))
                .isInstanceOf(RecallProviderUnavailableException.class);
    }
}
