
package com.smartshopping.checkout_service.service;

import com.smartshopping.checkout_service.dto.PurchaseRecordRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RecommendationClient {

    private final RestClient restClient;

    public RecommendationClient(
            @Value("${app.recommendation-service.base-url:http://localhost:8085}")
            String baseUrl
    ) {
        this.restClient = RestClient.create(baseUrl);
    }

    public void recordPurchase(
            String authorization,
            PurchaseRecordRequest request
    ) {
        restClient.post()
                .uri("/api/recommendations/purchases")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
