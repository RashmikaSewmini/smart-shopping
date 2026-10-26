
package com.smartshopping.product_service.controller;

import com.smartshopping.product_service.dto.PromotionRequest;
import com.smartshopping.product_service.dto.PromotionResponse;
import com.smartshopping.product_service.entity.Promotion;
import com.smartshopping.product_service.service.PromotionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Promotion createPromotion(
            @Valid @RequestBody PromotionRequest request
    ) {
        return promotionService.createPromotion(request);
    }

    @GetMapping("/active")
    public List<PromotionResponse> getActivePromotions() {
        return promotionService.getActivePromotions();
    }

    @GetMapping("/product/{productId}")
    public List<PromotionResponse> getActivePromotionsForProduct(
            @PathVariable Long productId
    ) {
        return promotionService.getActivePromotionsForProduct(productId);
    }
}
