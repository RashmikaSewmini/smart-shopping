
package com.smartshopping.product_service.dto;

import com.smartshopping.product_service.entity.Promotion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionRequest(
        @NotNull
        @Positive
        Long productId,

        @NotNull
        Promotion.DiscountType discountType,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal discountValue,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {
}
