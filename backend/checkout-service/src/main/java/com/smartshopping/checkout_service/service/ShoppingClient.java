package com.smartshopping.checkout_service.service;

import com.smartshopping.checkout_service.dto.ShoppingSessionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class ShoppingClient {

    private final RestClient restClient;

    public ShoppingClient(
            RestClient.Builder builder,
            @Value("${shopping-service.base-url}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public ShoppingSessionResponse getSession(Long sessionId) {
        try {
            ShoppingSessionResponse response = restClient.get()
                    .uri("/api/shopping-sessions/{sessionId}", sessionId)
                    .retrieve()
                    .body(ShoppingSessionResponse.class);

            if (response == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Shopping Service returned an empty response"
                );
            }

            return response;

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shopping session not found: " + sessionId
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Shopping Service returned HTTP "
                            + ex.getStatusCode().value()
            );
        }
    }

    public void completeSession(Long sessionId) {
        try {
            restClient.post()
                    .uri("/api/shopping-sessions/{sessionId}/complete",
                            sessionId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shopping session not found: " + sessionId
                );
            }

            if (ex.getStatusCode().value() == 409) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Shopping session is no longer active"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not complete the shopping session. Shopping Service returned HTTP "
                            + ex.getStatusCode().value()
            );
        }
    }
}