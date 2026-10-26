
package com.smartshopping.shopping_service.service;

import com.smartshopping.shopping_service.client.ProductClient;
import com.smartshopping.shopping_service.dto.AddManualItemRequest;
import com.smartshopping.shopping_service.dto.ProductResponse;
import com.smartshopping.shopping_service.dto.ShoppingItemResponse;
import com.smartshopping.shopping_service.dto.ShoppingSessionResponse;
import com.smartshopping.shopping_service.entity.ShoppingItem;
import com.smartshopping.shopping_service.entity.ShoppingSession;
import com.smartshopping.shopping_service.repository.ShoppingItemRepository;
import com.smartshopping.shopping_service.repository.ShoppingSessionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ShoppingService {

    private static final Logger log =
            LoggerFactory.getLogger(ShoppingService.class);

    private final ShoppingSessionRepository sessionRepository;
    private final ShoppingItemRepository itemRepository;
    private final ProductClient productClient;

    public ShoppingService(
            ShoppingSessionRepository sessionRepository,
            ShoppingItemRepository itemRepository,
            ProductClient productClient
    ) {
        this.sessionRepository = sessionRepository;
        this.itemRepository = itemRepository;
        this.productClient = productClient;
    }

    @Transactional
    public ShoppingSessionResponse createSession() {
        ShoppingSession session = new ShoppingSession();
        session.setStatus("ACTIVE");

        ShoppingSession saved = sessionRepository.save(session);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ShoppingSessionResponse getSession(Long sessionId) {
        ShoppingSession session = getSessionOrThrow(sessionId);
        return toResponse(session);
    }

    @Transactional
    public ShoppingSessionResponse addProductByBarcode(
            Long sessionId,
            String barcode
    ) {
        ShoppingSession session = getActiveSession(sessionId);

        if (barcode == null || barcode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Barcode is required"
            );
        }

        ProductResponse product =
                productClient.getProductByBarcode(barcode.trim());

        if (product == null
                || product.id() == null
                || product.id() <= 0
                || product.name() == null
                || product.name().isBlank()
                || product.price() == null
                || product.price().signum() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Product Service returned invalid product data"
            );
        }

        ShoppingItem item = new ShoppingItem();
        item.setProductId(product.id());
        item.setProductName(product.name().trim());
        item.setUnitPrice(product.price());
        item.setQuantity(1);

        session.addItem(item);

        ShoppingSession saved = sessionRepository.save(session);

        log.info(
                "Added catalog product to session {}: productId={}, name={}",
                sessionId,
                product.id(),
                product.name()
        );

        return toResponse(saved);
    }

    @Transactional
    public ShoppingSessionResponse addManualItem(
            Long sessionId,
            AddManualItemRequest request
    ) {
        ShoppingSession session = getActiveSession(sessionId);

        if (request == null
                || request.productName() == null
                || request.productName().isBlank()
                || request.unitPrice() == null
                || request.unitPrice().signum() < 0
                || request.quantity() == null
                || request.quantity() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid manual item name, price and quantity are required"
            );
        }

        ShoppingItem item = new ShoppingItem();

        // Manual items do not have a Product Service ID.
        item.setProductId(null);
        item.setProductName(request.productName().trim());
        item.setUnitPrice(request.unitPrice());
        item.setQuantity(request.quantity());

        session.addItem(item);

        ShoppingSession saved = sessionRepository.save(session);

        log.info(
                "Added manual item to session {}: name={}, productId=null",
                sessionId,
                item.getProductName()
        );

        return toResponse(saved);
    }

    @Transactional
    public ShoppingSessionResponse updateQuantity(
            Long sessionId,
            Long itemId,
            Integer quantity
    ) {
        getActiveSession(sessionId);

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        ShoppingItem item = itemRepository
                .findByIdAndShoppingSession_Id(itemId, sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shopping item not found in this session"
                ));

        item.setQuantity(quantity);
        itemRepository.save(item);

        return toResponse(getSessionOrThrow(sessionId));
    }

    @Transactional
    public ShoppingSessionResponse removeItem(
            Long sessionId,
            Long itemId
    ) {
        ShoppingSession session = getActiveSession(sessionId);

        ShoppingItem item = itemRepository
                .findByIdAndShoppingSession_Id(itemId, sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shopping item not found in this session"
                ));

        session.removeItem(item);
        sessionRepository.save(session);

        return toResponse(session);
    }

    @Transactional
    public ShoppingSessionResponse completeSession(Long sessionId) {
        ShoppingSession session = getActiveSession(sessionId);

        session.setStatus("COMPLETED");
        ShoppingSession saved = sessionRepository.save(session);

        return toResponse(saved);
    }

    private ShoppingSession getActiveSession(Long sessionId) {
        ShoppingSession session = getSessionOrThrow(sessionId);

        if (!"ACTIVE".equalsIgnoreCase(session.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Shopping session is already completed"
            );
        }

        return session;
    }

    private ShoppingSession getSessionOrThrow(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shopping session not found"
                ));
    }

    private ShoppingSessionResponse toResponse(
            ShoppingSession session
    ) {
        List<ShoppingItemResponse> items = session.getItems()
                .stream()
                .map(item -> {
                    log.debug(
                            "Returning shopping item: itemId={}, productId={}, name={}",
                            item.getId(),
                            item.getProductId(),
                            item.getProductName()
                    );

                    BigDecimal lineTotal = item.getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(item.getQuantity())
                            );

                    return new ShoppingItemResponse(
                            item.getId(),
                            item.getProductId(),
                            item.getProductName(),
                            item.getUnitPrice(),
                            item.getQuantity(),
                            lineTotal
                    );
                })
                .toList();

        BigDecimal total = items.stream()
                .map(ShoppingItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ShoppingSessionResponse(
                session.getId(),
                session.getStatus(),
                session.getCreatedAt(),
                items,
                total
        );
    }
}
