package com.smartshopping.product_service.service;

import com.smartshopping.product_service.entity.Product;
import com.smartshopping.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(Product product) {
        if (repository.existsByBarcode(product.getBarcode())) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "A product with this barcode already exists"
            );
        }

        product.setActive(true);
        return repository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getAll() {
        return repository.findAllByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return repository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Product not found"
                ));
    }

    @Transactional(readOnly = true)
    public Product getByBarcode(String barcode) {
        return repository.findByBarcodeAndActiveTrue(barcode)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Product not found for this barcode"
                ));
    }

    public Product update(Long id, Product updated) {
        Product existing = getById(id);

        repository.findAll().stream()
                .filter(p -> p.getBarcode().equals(updated.getBarcode()))
                .filter(p -> !p.getId().equals(id))
                .findFirst()
                .ifPresent(p -> {
                    throw new ResponseStatusException(
                            CONFLICT,
                            "Another product already uses this barcode"
                    );
                });

        existing.setName(updated.getName());
        existing.setBarcode(updated.getBarcode());
        existing.setPrice(updated.getPrice());

        return repository.save(existing);
    }

    public void delete(Long id) {
        Product product = getById(id);
        product.setActive(false);
        repository.save(product);
    }
}