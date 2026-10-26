
package com.smartshopping.auth_service.dto;

public record AuthResponse(
        String token,
        Long customerId,
        String name,
        String email
) {}
