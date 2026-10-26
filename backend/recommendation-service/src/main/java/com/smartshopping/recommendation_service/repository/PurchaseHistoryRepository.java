
package com.smartshopping.recommendation_service.repository;

import com.smartshopping.recommendation_service.entity.PurchaseHistoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseHistoryRepository
        extends JpaRepository<PurchaseHistoryItem, Long> {

    List<PurchaseHistoryItem>
    findByCustomerIdOrderByPurchasedAtDesc(Long customerId);

    boolean existsByCustomerIdAndPurchaseId(
            Long customerId,
            String purchaseId
    );

    List<PurchaseHistoryItem> findByCustomerId(Long customerId);
}
