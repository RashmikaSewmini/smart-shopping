package com.smartshopping.shopping_service.repository;

import com.smartshopping.shopping_service.entity.ShoppingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingSessionRepository
        extends JpaRepository<ShoppingSession, Long> {
}