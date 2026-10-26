
package com.smartshopping.checkout_service.service;

import com.smartshopping.checkout_service.dto.CheckoutResponse;
import com.smartshopping.checkout_service.dto.CreateCheckoutRequest;
import com.smartshopping.checkout_service.dto.PurchaseRecordRequest;
import com.smartshopping.checkout_service.dto.ShoppingItemResponse;
import com.smartshopping.checkout_service.dto.ShoppingSessionResponse;
import com.smartshopping.checkout_service.entity.Checkout;
import com.smartshopping.checkout_service.repository.CheckoutRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CheckoutService {

    private static final Logger log =
            LoggerFactory.getLogger(CheckoutService.class);

    private final CheckoutRepository checkoutRepository;
    private final ShoppingClient shoppingClient;
    private final RecommendationClient recommendationClient;

    public CheckoutService(
            CheckoutRepository checkoutRepository,
            ShoppingClient shoppingClient,
            RecommendationClient recommendationClient
    ) {
        this.checkoutRepository = checkoutRepository;
        this.shoppingClient = shoppingClient;
        this.recommendationClient = recommendationClient;
    }

    @Transactional
    public CheckoutResponse createCheckout(
            CreateCheckoutRequest request,
            String authorization
    ) {
        if (authorization == null
                || !authorization.startsWith("Bearer ")
                || authorization.substring(7).trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "A valid Bearer token is required"
            );
        }

        if (request == null
                || request.shoppingSessionId() == null
                || request.amountTendered() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Shopping session ID and amount tendered are required"
            );
        }

        Long sessionId = request.shoppingSessionId();
        BigDecimal amountTendered = request.amountTendered();

        if (amountTendered.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount tendered cannot be negative"
            );
        }

        if (checkoutRepository.existsByShoppingSessionId(sessionId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This shopping session has already been checked out"
            );
        }

        ShoppingSessionResponse session =
                shoppingClient.getSession(sessionId);

        if (session == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Shopping Service returned no session"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(session.status())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only active shopping sessions can be checked out"
            );
        }

        if (session.items() == null || session.items().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot checkout an empty shopping session"
            );
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShoppingItemResponse item : session.items()) {
            if (item == null
                    || item.unitPrice() == null
                    || item.quantity() == null
                    || item.unitPrice().signum() < 0
                    || item.quantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Shopping Service returned invalid item data"
                );
            }

            log.info(
                    "Shopping item received: itemId={}, productId={}, name={}",
                    item.id(),
                    item.productId(),
                    item.productName()
            );

            totalAmount = totalAmount.add(
                    item.unitPrice().multiply(
                            BigDecimal.valueOf(item.quantity())
                    )
            );
        }

        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal tendered = amountTendered.setScale(
                2,
                RoundingMode.HALF_UP
        );

        if (tendered.compareTo(totalAmount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient payment. Amount due: " + totalAmount
            );
        }

        BigDecimal changeAmount = tendered.subtract(totalAmount);

        Checkout checkout = new Checkout();
        checkout.setShoppingSessionId(sessionId);
        checkout.setTotalAmount(totalAmount);
        checkout.setAmountTendered(tendered);
        checkout.setChangeAmount(changeAmount);
        checkout.setStatus("COMPLETED");

        try {
            checkout = checkoutRepository.saveAndFlush(checkout);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This shopping session has already been checked out",
                    ex
            );
        }

        log.info(
                "Checkout {} saved for shopping session {}",
                checkout.getId(),
                sessionId
        );

        // Complete the session after the checkout has been saved.
        shoppingClient.completeSession(sessionId);

        // Send catalog and manually entered items to purchase history.
        // A null productId identifies a manual item; Recommendation Service
        // stores it for history/summary but excludes it from catalog recommendations.
        List<PurchaseRecordRequest.PurchaseItem> purchaseItems =
                session.items().stream()
                        .map(item -> new PurchaseRecordRequest.PurchaseItem(
                                item.productId(),
                                item.productName(),
                                item.unitPrice(),
                                item.quantity()
                        ))
                        .toList();

        if (purchaseItems.isEmpty()) {
            log.warn(
                    "Checkout {} has no items to record in purchase history.",
                    checkout.getId()
            );
        } else {
            PurchaseRecordRequest purchaseRequest =
                    new PurchaseRecordRequest(
                            String.valueOf(checkout.getId()),
                            purchaseItems
                    );

            try {
                recommendationClient.recordPurchase(authorization, purchaseRequest);
                log.info(
                        "Purchase history recorded for checkout {}",
                        checkout.getId()
                );
            } catch (RestClientException ex) {
                log.error(
                        "Checkout {} completed, but purchase history could not be recorded: {}",
                        checkout.getId(), ex.getMessage(), ex
                );
            }
        }

        return toResponse(checkout);
    }

    @Transactional(readOnly = true)
    public CheckoutResponse getCheckout(Long checkoutId) {
        Checkout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Checkout not found: " + checkoutId
                ));

        return toResponse(checkout);
    }

    @Transactional(readOnly = true)
    public CheckoutResponse getCheckoutBySessionId(Long sessionId) {
        Checkout checkout = checkoutRepository
                .findByShoppingSessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No checkout found for shopping session: " + sessionId
                ));

        return toResponse(checkout);
    }

    private CheckoutResponse toResponse(Checkout checkout) {
        return new CheckoutResponse(
                checkout.getId(),
                checkout.getShoppingSessionId(),
                checkout.getTotalAmount(),
                checkout.getAmountTendered(),
                checkout.getChangeAmount(),
                checkout.getStatus(),
                checkout.getCreatedAt()
        );
    }
}
