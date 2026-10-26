package com.smartshopping.shopping_service.repository;

import com.smartshopping.shopping_service.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingItemRepository
        extends JpaRepository<ShoppingItem, Long> {

    Optional<ShoppingItem> findByIdAndShoppingSession_Id(
            Long itemId,
            Long sessionId
    );
}