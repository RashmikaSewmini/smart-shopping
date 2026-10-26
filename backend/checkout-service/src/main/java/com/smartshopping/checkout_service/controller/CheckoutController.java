
package com.smartshopping.checkout_service.controller;

import com.smartshopping.checkout_service.dto.CheckoutResponse;
import com.smartshopping.checkout_service.dto.CreateCheckoutRequest;
import com.smartshopping.checkout_service.service.CheckoutService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkouts")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    public ResponseEntity<CheckoutResponse> createCheckout(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization,
            @Valid @RequestBody CreateCheckoutRequest request
    ) {
        CheckoutResponse response =
                checkoutService.createCheckout(request, authorization);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{checkoutId}")
    public ResponseEntity<CheckoutResponse> getCheckout(
            @PathVariable Long checkoutId
    ) {
        return ResponseEntity.ok(
                checkoutService.getCheckout(checkoutId)
        );
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<CheckoutResponse> getCheckoutBySessionId(
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(
                checkoutService.getCheckoutBySessionId(sessionId)
        );
    }
}
