
package com.smartshopping.recommendation_service.dto;

import java.math.BigDecimal;

public record ProductCatalogItem(
        Long id,
        String name,
        String barcode,
        BigDecimal price,
        boolean active
) {
}