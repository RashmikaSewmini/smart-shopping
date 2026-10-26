package com.smartshopping.shopping_service.controller;

import com.smartshopping.shopping_service.dto.AddManualItemRequest;
import com.smartshopping.shopping_service.dto.ShoppingSessionResponse;
import com.smartshopping.shopping_service.dto.UpdateQuantityRequest;
import com.smartshopping.shopping_service.service.ShoppingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shopping-sessions")
public class ShoppingController {

    private final ShoppingService shoppingService;

    public ShoppingController(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingSessionResponse createSession() {
        return shoppingService.createSession();
    }

    @GetMapping("/{sessionId}")
    public ShoppingSessionResponse getSession(
            @PathVariable Long sessionId) {

        return shoppingService.getSession(sessionId);
    }

    @PostMapping("/{sessionId}/items/barcode/{barcode}")
    public ShoppingSessionResponse addProductByBarcode(
            @PathVariable Long sessionId,
            @PathVariable String barcode) {

        return shoppingService.addProductByBarcode(
                sessionId,
                barcode
        );
    }

    @PostMapping("/{sessionId}/items/manual")
    public ShoppingSessionResponse addManualItem(
            @PathVariable Long sessionId,
            @Valid @RequestBody AddManualItemRequest request) {

        return shoppingService.addManualItem(
                sessionId,
                request
        );
    }

    @PatchMapping("/{sessionId}/items/{itemId}/quantity")
    public ShoppingSessionResponse updateQuantity(
            @PathVariable Long sessionId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateQuantityRequest request) {

        return shoppingService.updateQuantity(
                sessionId,
                itemId,
                request.quantity()
        );
    }

    @DeleteMapping("/{sessionId}/items/{itemId}")
    public ShoppingSessionResponse removeItem(
            @PathVariable Long sessionId,
            @PathVariable Long itemId) {

        return shoppingService.removeItem(sessionId, itemId);
    }

    @PostMapping("/{sessionId}/complete")
    public ShoppingSessionResponse completeSession(
            @PathVariable Long sessionId) {

        return shoppingService.completeSession(sessionId);
    }
}