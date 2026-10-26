package com.smartshopping.checkout_service.dto;

import java.math.BigDecimal;

public record ShoppingItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}