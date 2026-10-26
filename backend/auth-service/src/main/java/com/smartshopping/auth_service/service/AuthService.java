
package com.smartshopping.auth_service.service;

import com.smartshopping.auth_service.dto.AuthResponse;
import com.smartshopping.auth_service.dto.LoginRequest;
import com.smartshopping.auth_service.dto.RegisterRequest;
import com.smartshopping.auth_service.entity.Customer;
import com.smartshopping.auth_service.repository.CustomerRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            CustomerRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email is already registered"
            );
        }

        Customer customer = repository.save(
                new Customer(
                        request.name().trim(),
                        email,
                        passwordEncoder.encode(request.password())
                )
        );

        return response(customer);
    }

    public AuthResponse login(LoginRequest request) {
        Customer customer = repository
                .findByEmailIgnoreCase(request.email().trim())
                .filter(c -> passwordEncoder.matches(
                        request.password(), c.getPasswordHash()
                ))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"
                ));

        return response(customer);
    }

    public Customer getCustomerFromToken(String token) {
        Claims claims = jwtService.parseToken(token);

        Long customerId = Long.valueOf(claims.getSubject());

        return repository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"
                ));
    }

    private AuthResponse response(Customer customer) {
        String token = jwtService.generateToken(
                customer.getId(), customer.getEmail()
        );

        return new AuthResponse(
                token, customer.getId(), customer.getName(), customer.getEmail()
        );
    }
}
