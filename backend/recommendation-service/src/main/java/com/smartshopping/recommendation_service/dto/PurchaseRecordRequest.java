package com.smartshopping.recommendation_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseRecordRequest(
        @NotBlank
        @Size(max = 100)
        String purchaseId,

        @NotEmpty
        @Valid
        List<PurchaseItemRequest> items
) {
    public record PurchaseItemRequest(
            // Null for manually entered items that are not in the catalog.
            Long productId,

            @NotBlank
            @Size(max = 200)
            String productName,

            @NotNull
            @DecimalMin("0.00")
            BigDecimal unitPrice,

            @NotNull
            @Min(1)
            Integer quantity
    ) {
    }
}
