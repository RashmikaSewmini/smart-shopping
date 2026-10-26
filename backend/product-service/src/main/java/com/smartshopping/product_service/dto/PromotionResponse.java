
package com.smartshopping.product_service.dto;

import com.smartshopping.product_service.entity.Promotion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionResponse(
        Long promotionId,
        Long productId,
        String productName,
        BigDecimal regularPrice,
        Promotion.DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal discountedPrice,
        BigDecimal savings,
        LocalDate startDate,
        LocalDate endDate
) {
}
