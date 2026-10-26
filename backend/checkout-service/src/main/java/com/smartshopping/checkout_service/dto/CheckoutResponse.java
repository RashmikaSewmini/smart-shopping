package com.smartshopping.checkout_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckoutResponse(
        Long id,
        Long shoppingSessionId,
        BigDecimal totalAmount,
        BigDecimal amountTendered,
        BigDecimal changeAmount,
        String status,
        LocalDateTime createdAt
) {
}