
package com.example.expensetracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class FredService {

    private final RestClient restClient;
    private final String apiKey;

    public FredService(
            @Value("${fred.api.base-url:https://api.stlouisfed.org/fred}")
            String baseUrl,
            @Value("${fred.api.key}") String apiKey) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.apiKey = apiKey;
    }

    public Map<?, ?> getEconomicData(String seriesId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/series/observations")
                        .queryParam("series_id", seriesId)
                        .queryParam("api_key", apiKey)
                        .queryParam("file_type", "json")
                        .build())
                .retrieve()
                .body(Map.class);
    }
}
