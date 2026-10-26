
package com.smartshopping.auth_service.controller;

import com.smartshopping.auth_service.dto.AuthResponse;
import com.smartshopping.auth_service.dto.LoginRequest;
import com.smartshopping.auth_service.dto.RegisterRequest;
import com.smartshopping.auth_service.entity.Customer;
import com.smartshopping.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public Map<String, Object> me(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Bearer token is required"
            );
        }

        Customer customer = authService.getCustomerFromToken(
                authorization.substring(7)
        );

        return Map.of(
                "id", customer.getId(),
                "name", customer.getName(),
                "email", customer.getEmail(),
                "createdAt", customer.getCreatedAt()
        );
    }
}
