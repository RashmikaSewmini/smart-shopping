package com.smartshopping.shopping_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShoppingSessionResponse(
        Long id,
        String status,
        LocalDateTime createdAt,
        List<ShoppingItemResponse> items,
        BigDecimal total
) {
}