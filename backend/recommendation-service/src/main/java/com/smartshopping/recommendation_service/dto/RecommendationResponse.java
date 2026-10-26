
package com.smartshopping.recommendation_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecommendationResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        String discountType,
        BigDecimal discountValue,
        BigDecimal discountedPrice,
        BigDecimal savings,
        int purchaseCount,
        int totalQuantityPurchased,
        LocalDateTime lastPurchasedAt,
        double score,
        String reason
) {
}
