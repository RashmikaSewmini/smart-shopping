package com.smartshopping.shopping_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_sessions")
public class ShoppingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "shoppingSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ShoppingItem> items = new ArrayList<>();

    public ShoppingSession() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void addItem(ShoppingItem item) {
        items.add(item);
        item.setShoppingSession(this);
    }

    public void removeItem(ShoppingItem item) {
        items.remove(item);
        item.setShoppingSession(null);
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ShoppingItem> getItems() {
        return items;
    }
}