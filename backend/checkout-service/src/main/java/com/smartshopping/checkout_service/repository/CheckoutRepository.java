package com.smartshopping.checkout_service.repository;

import com.smartshopping.checkout_service.entity.Checkout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckoutRepository
        extends JpaRepository<Checkout, Long> {

    Optional<Checkout> findByShoppingSessionId(Long shoppingSessionId);

    boolean existsByShoppingSessionId(Long shoppingSessionId);
}