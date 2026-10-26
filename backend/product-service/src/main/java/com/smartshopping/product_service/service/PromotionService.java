
package com.smartshopping.product_service.service;

import com.smartshopping.product_service.dto.PromotionRequest;
import com.smartshopping.product_service.dto.PromotionResponse;
import com.smartshopping.product_service.entity.Product;
import com.smartshopping.product_service.entity.Promotion;
import com.smartshopping.product_service.repository.ProductRepository;
import com.smartshopping.product_service.repository.PromotionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    public PromotionService(
            PromotionRepository promotionRepository,
            ProductRepository productRepository
    ) {
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Promotion createPromotion(PromotionRequest request) {
        validateDates(request.startDate(), request.endDate());

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot create a promotion for an inactive product"
            );
        }

        validateDiscount(
                request.discountType(),
                request.discountValue(),
                product.getPrice()
        );

        Promotion promotion = new Promotion();
        promotion.setProductId(request.productId());
        promotion.setDiscountType(request.discountType());
        promotion.setDiscountValue(request.discountValue());
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());
        promotion.setActive(true);

        return promotionRepository.save(promotion);
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotions() {
        LocalDate today = LocalDate.now();

        List<Promotion> promotions =
                promotionRepository.findActivePromotions(today);

        Map<Long, List<Promotion>> promotionsByProduct =
                promotions.stream().collect(
                        Collectors.groupingBy(Promotion::getProductId)
                );

        List<PromotionResponse> results = new ArrayList<>();

        for (Map.Entry<Long, List<Promotion>> entry
                : promotionsByProduct.entrySet()) {

            Product product = productRepository.findById(entry.getKey())
                    .filter(Product::isActive)
                    .orElse(null);

            if (product == null) {
                continue;
            }

            entry.getValue().stream()
                    .map(promotion -> toResponse(promotion, product))
                    .min(Comparator.comparing(
                            PromotionResponse::discountedPrice
                    ))
                    .ifPresent(results::add);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotionsForProduct(
            Long productId
    ) {
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active product not found"
                ));

        return promotionRepository
                .findActivePromotionsForProduct(productId, LocalDate.now())
                .stream()
                .map(promotion -> toResponse(promotion, product))
                .sorted(Comparator.comparing(
                        PromotionResponse::discountedPrice
                ))
                .toList();
    }

    private PromotionResponse toResponse(
            Promotion promotion,
            Product product
    ) {
        BigDecimal regularPrice = product.getPrice();
        BigDecimal savings;

        if (promotion.getDiscountType()
                == Promotion.DiscountType.PERCENTAGE) {

            savings = regularPrice
                    .multiply(promotion.getDiscountValue())
                    .divide(
                            new BigDecimal("100"),
                            2,
                            RoundingMode.HALF_UP
                    );
        } else {
            savings = promotion.getDiscountValue()
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal discountedPrice = regularPrice
                .subtract(savings)
                .setScale(2, RoundingMode.HALF_UP);

        return new PromotionResponse(
                promotion.getId(),
                product.getId(),
                product.getName(),
                regularPrice,
                promotion.getDiscountType(),
                promotion.getDiscountValue(),
                discountedPrice,
                savings,
                promotion.getStartDate(),
                promotion.getEndDate()
        );
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date must be on or after start date"
            );
        }
    }

    private void validateDiscount(
            Promotion.DiscountType type,
            BigDecimal value,
            BigDecimal regularPrice
    ) {
        if (type == Promotion.DiscountType.PERCENTAGE
                && value.compareTo(new BigDecimal("100")) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Percentage discount cannot exceed 100"
            );
        }

        if (type == Promotion.DiscountType.FIXED_AMOUNT
                && value.compareTo(regularPrice) >= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fixed discount must be less than the product price"
            );
        }
    }
}
