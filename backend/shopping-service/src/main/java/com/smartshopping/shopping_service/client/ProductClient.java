package com.smartshopping.shopping_service.client;

import com.smartshopping.shopping_service.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(
            RestClient.Builder builder,
            @Value("${product-service.base-url}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public ProductResponse getProductByBarcode(String barcode) {
        try {
            ProductResponse product = restClient.get()
                    .uri("/api/products/barcode/{barcode}", barcode)
                    .retrieve()
                    .body(ProductResponse.class);

            if (product == null || !product.active()) {
                throw new IllegalArgumentException(
                        "Product is unavailable"
                );
            }

            return product;

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new IllegalArgumentException(
                        "No registered product found for barcode: " + barcode
                );
            }

            throw new IllegalStateException(
                    "Product Service returned HTTP " +
                            ex.getStatusCode().value(),
                    ex
            );
        }
    }
}