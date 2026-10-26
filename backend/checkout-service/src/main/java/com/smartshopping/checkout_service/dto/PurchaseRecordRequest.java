
package com.smartshopping.checkout_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseRecordRequest(
        String purchaseId,
        List<PurchaseItem> items
) {
    public record PurchaseItem(
            Long productId,
            String productName,
            BigDecimal unitPrice,
            Integer quantity
    ) {
    }
}
