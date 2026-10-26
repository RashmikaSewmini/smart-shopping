
package com.smartshopping.recommendation_service.controller;

import com.smartshopping.recommendation_service.dto.PurchaseRecordRequest;
import com.smartshopping.recommendation_service.dto.RecommendationResponse;
import com.smartshopping.recommendation_service.entity.PurchaseHistoryItem;
import com.smartshopping.recommendation_service.service.JwtService;
import com.smartshopping.recommendation_service.service.RecommendationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final JwtService jwtService;

    public RecommendationController(
            RecommendationService recommendationService,
            JwtService jwtService
    ) {
        this.recommendationService = recommendationService;
        this.jwtService = jwtService;
    }

    private Long getAuthenticatedCustomerId(String authorization) {
        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Bearer token is required"
            );
        }

        String token = authorization.substring(7).trim();

        if (token.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Bearer token is required"
            );
        }

        try {
            return jwtService.getCustomerId(token);
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token"
            );
        }
    }

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> recordPurchase(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @Valid @RequestBody PurchaseRecordRequest request
    ) {
        Long customerId = getAuthenticatedCustomerId(authorization);

        recommendationService.recordPurchase(customerId, request);

        return Map.of(
                "message", "Purchase history recorded successfully"
        );
    }

    @GetMapping
    public List<RecommendationResponse> getRecommendations(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {
        Long customerId = getAuthenticatedCustomerId(authorization);

        return recommendationService.getRecommendations(customerId);
    }

    @GetMapping("/history")
    public List<PurchaseHistoryItem> getHistory(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {
        Long customerId = getAuthenticatedCustomerId(authorization);

        return recommendationService.getHistory(customerId);
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {
        Long customerId = getAuthenticatedCustomerId(authorization);

        return recommendationService.getSummary(customerId);
    }
}
