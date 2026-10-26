
package com.smartshopping.product_service.dto;

public record PublicProductResponse(
        String barcode,
        String productName,
        String brands,
        String quantity,
        String imageUrl,
        boolean found
) {
}
