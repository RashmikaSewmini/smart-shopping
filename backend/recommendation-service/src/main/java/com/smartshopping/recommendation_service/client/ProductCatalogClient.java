
package com.smartshopping.recommendation_service.client;

import com.smartshopping.recommendation_service.dto.ProductCatalogItem;
import com.smartshopping.recommendation_service.dto.PromotionOffer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Component
public class ProductCatalogClient {

    private final RestClient restClient;

    public ProductCatalogClient(
            @Value("${app.product-service.base-url:http://localhost:8081}")
            String baseUrl
    ) {
        this.restClient = RestClient.create(baseUrl);
    }

    public List<ProductCatalogItem> getProducts() {
        try {
            ProductCatalogItem[] response = restClient.get()
                    .uri("/api/products")
                    .retrieve()
                    .body(ProductCatalogItem[].class);

            return response == null
                    ? List.of()
                    : Arrays.asList(response);

        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to retrieve products from Product Service",
                    exception
            );
        }
    }

    public List<PromotionOffer> getActivePromotions() {
        try {
            PromotionOffer[] response = restClient.get()
                    .uri("/api/promotions/active")
                    .retrieve()
                    .body(PromotionOffer[].class);

            return response == null
                    ? List.of()
                    : Arrays.asList(response);

        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to retrieve promotions from Product Service",
                    exception
            );
        }
    }
}
