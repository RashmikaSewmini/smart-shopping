package com.smartshopping.shopping_service.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String barcode,
        BigDecimal price,
        boolean active
) {
}