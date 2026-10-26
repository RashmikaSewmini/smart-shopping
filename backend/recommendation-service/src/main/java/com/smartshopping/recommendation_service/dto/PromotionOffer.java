
package com.smartshopping.recommendation_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionOffer(
        Long promotionId,
        Long productId,
        String productName,
        BigDecimal regularPrice,
        String discountType,
        BigDecimal discountValue,
        BigDecimal discountedPrice,
        BigDecimal savings,
        LocalDate startDate,
        LocalDate endDate
) {
}