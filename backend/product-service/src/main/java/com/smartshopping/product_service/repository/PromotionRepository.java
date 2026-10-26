
package com.smartshopping.product_service.repository;

import com.smartshopping.product_service.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository
        extends JpaRepository<Promotion, Long> {

    List<Promotion> findByProductId(Long productId);

    @Query("""
        SELECT p FROM Promotion p
        WHERE p.active = true
          AND p.startDate <= :today
          AND p.endDate >= :today
        ORDER BY p.endDate ASC
        """)
    List<Promotion> findActivePromotions(
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT p FROM Promotion p
        WHERE p.productId = :productId
          AND p.active = true
          AND p.startDate <= :today
          AND p.endDate >= :today
        ORDER BY p.endDate ASC
        """)
    List<Promotion> findActivePromotionsForProduct(
            @Param("productId") Long productId,
            @Param("today") LocalDate today
    );
}
