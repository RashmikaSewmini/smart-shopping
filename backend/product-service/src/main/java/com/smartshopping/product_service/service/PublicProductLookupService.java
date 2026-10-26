
package com.smartshopping.product_service.service;

import tools.jackson.databind.JsonNode;
import com.smartshopping.product_service.dto.PublicProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PublicProductLookupService {

    private final RestClient restClient;

    public PublicProductLookupService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://world.openfoodfacts.org")
                .defaultHeader(
                        "User-Agent",
                        "SmartShoppingApp/1.0 (your-real-email@example.com)"
                )
                .build();
    }


    public PublicProductResponse findByBarcode(String barcode) {

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v2/product/{barcode}.json")
                            .queryParam(
                                    "fields",
                                    "code,product_name,brands,quantity,image_url"
                            )
                            .build(barcode))
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.path("status").asInt() != 1) {
                return notFound(barcode);
            }

            JsonNode product = response.path("product");
            String name = product.path("product_name").asText("");

            if (name.isBlank()) {
                return notFound(barcode);
            }

            return new PublicProductResponse(
                    barcode,
                    name,
                    product.path("brands").asText(""),
                    product.path("quantity").asText(""),
                    product.path("image_url").asText(""),
                    true
            );

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return notFound(barcode);
        }
    }

    private PublicProductResponse notFound(String barcode) {
        return new PublicProductResponse(
                barcode, null, null, null, null, false
        );
    }

}
