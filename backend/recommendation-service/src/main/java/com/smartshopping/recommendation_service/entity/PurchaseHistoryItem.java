
package com.smartshopping.recommendation_service.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "purchase_history_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_customer_purchase_product",
                columnNames = {
                        "customer_id", "purchase_id", "product_id"
                }
        ),
        indexes = @Index(
                name = "idx_customer_purchase_date",
                columnList = "customer_id, purchased_at"
        )
)
public class PurchaseHistoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "purchase_id", nullable = false)
    private String purchaseId;

    @Column(name = "product_id", nullable = true)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    protected PurchaseHistoryItem() {
    }

    public PurchaseHistoryItem(
            Long customerId,
            String purchaseId,
            Long productId,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            LocalDateTime purchasedAt
    ) {
        this.customerId = customerId;
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.purchasedAt = purchasedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }
}
