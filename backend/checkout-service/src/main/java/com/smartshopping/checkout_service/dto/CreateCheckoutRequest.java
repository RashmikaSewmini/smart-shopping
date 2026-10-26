package com.smartshopping.checkout_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateCheckoutRequest(

        @NotNull(message = "Shopping session ID is required")
        @Positive(message = "Shopping session ID must be positive")
        Long shoppingSessionId,

        @NotNull(message = "Amount tendered is required")
        @DecimalMin(
                value = "0.00",
                message = "Amount tendered cannot be negative"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Amount tendered must have at most two decimal places"
        )
        BigDecimal amountTendered
) {
}