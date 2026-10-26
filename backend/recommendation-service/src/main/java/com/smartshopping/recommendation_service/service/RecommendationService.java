
package com.smartshopping.recommendation_service.service;

import com.smartshopping.recommendation_service.client.ProductCatalogClient;
import com.smartshopping.recommendation_service.dto.ProductCatalogItem;
import com.smartshopping.recommendation_service.dto.PromotionOffer;
import com.smartshopping.recommendation_service.dto.PurchaseRecordRequest;
import com.smartshopping.recommendation_service.dto.RecommendationResponse;
import com.smartshopping.recommendation_service.entity.PurchaseHistoryItem;
import com.smartshopping.recommendation_service.repository.PurchaseHistoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log =
            LoggerFactory.getLogger(RecommendationService.class);

    private final PurchaseHistoryRepository repository;
    private final ProductCatalogClient productCatalogClient;

    public RecommendationService(
            PurchaseHistoryRepository repository,
            ProductCatalogClient productCatalogClient
    ) {
        this.repository = repository;
        this.productCatalogClient = productCatalogClient;
    }

    @Transactional
    public void recordPurchase(
            Long customerId,
            PurchaseRecordRequest request
    ) {
        if (customerId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Customer ID is required"
            );
        }

        if (request == null
                || request.purchaseId() == null
                || request.purchaseId().isBlank()
                || request.purchaseId().trim().length() > 100
                || request.items() == null
                || request.items().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Purchase ID and at least one item are required"
            );
        }

        String purchaseId = request.purchaseId().trim();

        // Make retrying the same checkout safe.
        if (repository.existsByCustomerIdAndPurchaseId(
                customerId, purchaseId)) {
            log.info(
                    "Purchase {} already recorded for customer {}",
                    purchaseId, customerId
            );
            return;
        }

        // Catalog items: one row per product per checkout.
        Map<Long, PurchaseRecordRequest.PurchaseItemRequest> catalogItems =
                new LinkedHashMap<>();

        // Manual items have no catalog ID; preserve each item separately.
        List<PurchaseRecordRequest.PurchaseItemRequest> manualItems =
                new ArrayList<>();

        for (PurchaseRecordRequest.PurchaseItemRequest item
                : request.items()) {

            if (item == null
                    || item.productName() == null
                    || item.productName().isBlank()
                    || item.productName().trim().length() > 200
                    || item.unitPrice() == null
                    || item.unitPrice().signum() < 0
                    || item.quantity() == null
                    || item.quantity() <= 0
                    || (item.productId() != null
                    && item.productId() <= 0)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Purchase contains an invalid item"
                );
            }

            if (item.productId() == null) {
                manualItems.add(item);
                continue;
            }

            PurchaseRecordRequest.PurchaseItemRequest existing =
                    catalogItems.get(item.productId());

            if (existing == null) {
                catalogItems.put(item.productId(), item);
                continue;
            }

            if (existing.unitPrice().compareTo(item.unitPrice()) != 0
                    || !existing.productName().trim()
                    .equalsIgnoreCase(item.productName().trim())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate catalog product has inconsistent names or prices"
                );
            }

            long combinedQuantity =
                    (long) existing.quantity() + item.quantity();

            if (combinedQuantity > Integer.MAX_VALUE) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Combined quantity is too large for product ID "
                                + item.productId()
                );
            }

            catalogItems.put(
                    item.productId(),
                    new PurchaseRecordRequest.PurchaseItemRequest(
                            existing.productId(),
                            existing.productName().trim(),
                            existing.unitPrice(),
                            (int) combinedQuantity
                    )
            );
        }

        LocalDateTime purchasedAt = LocalDateTime.now();
        List<PurchaseHistoryItem> historyItems = new ArrayList<>();

        for (PurchaseRecordRequest.PurchaseItemRequest item
                : catalogItems.values()) {
            historyItems.add(new PurchaseHistoryItem(
                    customerId,
                    purchaseId,
                    item.productId(),
                    item.productName().trim(),
                    item.unitPrice(),
                    item.quantity(),
                    purchasedAt
            ));
        }

        for (PurchaseRecordRequest.PurchaseItemRequest item
                : manualItems) {
            historyItems.add(new PurchaseHistoryItem(
                    customerId,
                    purchaseId,
                    null,
                    item.productName().trim(),
                    item.unitPrice(),
                    item.quantity(),
                    purchasedAt
            ));
        }

        repository.saveAllAndFlush(historyItems);

        log.info(
                "Recorded {} history rows for customer {}, checkout {}: "
                        + "{} catalog products, {} manual items",
                historyItems.size(),
                customerId,
                purchaseId,
                catalogItems.size(),
                manualItems.size()
        );
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations(
            Long customerId
    ) {
        List<PurchaseHistoryItem> history =
                repository.findByCustomerIdOrderByPurchasedAtDesc(
                        customerId
                );

        List<ProductCatalogItem> products =
                productCatalogClient.getProducts()
                        .stream()
                        .filter(ProductCatalogItem::active)
                        .filter(product ->
                                product.id() != null
                                        && product.name() != null
                                        && product.price() != null
                                        && product.price().signum() >= 0
                        )
                        .toList();

        if (products.isEmpty()) {
            return List.of();
        }

        Map<Long, PromotionOffer> promotions =
                productCatalogClient.getActivePromotions()
                        .stream()
                        .filter(offer -> offer.productId() != null)
                        .filter(offer ->
                                offer.discountedPrice() != null
                                        && offer.savings() != null
                                        && offer.discountedPrice().signum() >= 0
                                        && offer.savings().signum() > 0
                        )
                        .filter(offer ->
                                offer.startDate() != null
                                        && offer.endDate() != null
                                        && !LocalDate.now().isBefore(
                                        offer.startDate())
                                        && !LocalDate.now().isAfter(
                                        offer.endDate())
                        )
                        .collect(Collectors.toMap(
                                PromotionOffer::productId,
                                Function.identity(),
                                (first, second) ->
                                        first.discountedPrice().compareTo(
                                                second.discountedPrice()
                                        ) <= 0 ? first : second
                        ));

        // Manual history rows are intentionally excluded here because
        // they have no catalog product ID.
        Map<Long, List<PurchaseHistoryItem>> historyByProduct =
                history.stream()
                        .filter(item -> item.getProductId() != null)
                        .collect(Collectors.groupingBy(
                                PurchaseHistoryItem::getProductId
                        ));

        List<RecommendationResponse> recommendations =
                new ArrayList<>();

        for (ProductCatalogItem product : products) {
            List<PurchaseHistoryItem> purchases =
                    historyByProduct.getOrDefault(
                            product.id(), List.of()
                    );

            PromotionOffer promotion = promotions.get(product.id());

            if (purchases.isEmpty() && promotion == null) {
                continue;
            }

            int purchaseCount = (int) purchases.stream()
                    .map(PurchaseHistoryItem::getPurchaseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            int totalQuantity = purchases.stream()
                    .mapToInt(PurchaseHistoryItem::getQuantity)
                    .filter(quantity -> quantity > 0)
                    .sum();

            LocalDateTime lastPurchasedAt = purchases.stream()
                    .map(PurchaseHistoryItem::getPurchasedAt)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            BigDecimal regularPrice = product.price();

            BigDecimal discountedPrice = promotion == null
                    ? regularPrice
                    : promotion.discountedPrice();

            BigDecimal savings = promotion == null
                    ? BigDecimal.ZERO.setScale(2)
                    : promotion.savings();

            double frequencyScore =
                    Math.min(100.0, purchaseCount * 25.0);

            double recencyScore = 0.0;

            if (lastPurchasedAt != null) {
                long daysSincePurchase = Math.max(
                        0,
                        ChronoUnit.DAYS.between(
                                lastPurchasedAt.toLocalDate(),
                                LocalDate.now()
                        )
                );

                recencyScore =
                        Math.max(0.0, 100.0 - daysSincePurchase * 3.0);
            }

            double quantityScore =
                    Math.min(100.0, totalQuantity * 10.0);

            double discountPercentage = regularPrice.signum() > 0
                    ? savings.multiply(new BigDecimal("100"))
                    .divide(
                            regularPrice,
                            4,
                            RoundingMode.HALF_UP
                    ).doubleValue()
                    : 0.0;

            double discountScore =
                    Math.min(100.0, discountPercentage * 5.0);

            double score =
                    frequencyScore * 0.40
                            + recencyScore * 0.25
                            + discountScore * 0.25
                            + quantityScore * 0.10;

            List<String> reasons = new ArrayList<>();

            if (purchaseCount > 1) {
                reasons.add("Frequently purchased");
            } else if (purchaseCount == 1) {
                reasons.add("Previously purchased");
            }

            if (lastPurchasedAt != null) {
                long daysSincePurchase = Math.max(
                        0,
                        ChronoUnit.DAYS.between(
                                lastPurchasedAt.toLocalDate(),
                                LocalDate.now()
                        )
                );

                if (daysSincePurchase <= 7) {
                    reasons.add("Purchased recently");
                }
            }

            if (promotion != null) {
                reasons.add(
                        "PERCENTAGE".equals(promotion.discountType())
                                ? "Discounted "
                                + promotion.discountValue()
                                + "% today"
                                : "Special offer: save Rs. "
                                + savings.toPlainString()
                );
            }

            if (reasons.isEmpty()) {
                reasons.add("Recommended for you");
            }

            recommendations.add(new RecommendationResponse(
                    product.id(),
                    product.name(),
                    regularPrice,
                    promotion == null ? null : promotion.discountType(),
                    promotion == null ? null : promotion.discountValue(),
                    discountedPrice,
                    savings,
                    purchaseCount,
                    totalQuantity,
                    lastPurchasedAt,
                    Math.round(score * 100.0) / 100.0,
                    String.join(" • ", reasons)
            ));
        }

        recommendations.sort(
                Comparator.comparingDouble(
                                RecommendationResponse::score
                        ).reversed()
                        .thenComparing(
                                RecommendationResponse::discountedPrice
                        )
        );

        return recommendations.stream()
                .limit(10)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseHistoryItem> getHistory(Long customerId) {
        return repository.findByCustomerIdOrderByPurchasedAtDesc(
                customerId
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSummary(Long customerId) {
        List<PurchaseHistoryItem> history =
                repository.findByCustomerId(customerId);

        BigDecimal totalSpent = history.stream()
                .filter(item ->
                        item.getUnitPrice() != null
                                && item.getQuantity() != null
                )
                .map(item -> item.getUnitPrice().multiply(
                        BigDecimal.valueOf(item.getQuantity())
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = history.stream()
                .filter(item -> item.getQuantity() != null)
                .mapToInt(PurchaseHistoryItem::getQuantity)
                .sum();

        long uniqueProducts = history.stream()
                .map(PurchaseHistoryItem::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long purchaseCount = history.stream()
                .map(PurchaseHistoryItem::getPurchaseId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("purchaseCount", purchaseCount);
        summary.put("uniqueProducts", uniqueProducts);
        summary.put("totalItemsPurchased", totalItems);
        summary.put("totalSpent", totalSpent);

        return summary;
    }
}
