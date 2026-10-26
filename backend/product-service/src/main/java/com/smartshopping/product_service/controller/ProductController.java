package com.smartshopping.product_service.controller;

import com.smartshopping.product_service.entity.Product;
import com.smartshopping.product_service.service.ProductService;
import com.smartshopping.product_service.service.PublicProductLookupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.smartshopping.product_service.dto.PublicProductResponse;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {


    private final ProductService service;
    private final PublicProductLookupService publicProductLookupService;

    public ProductController(
            ProductService productService,
            PublicProductLookupService publicProductLookupService) {

        this.service = productService;
        this.publicProductLookupService = publicProductLookupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody Product product) {
        return service.create(product);
    }

    @GetMapping
    public List<Product> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/barcode/{barcode}")
    public Product getByBarcode(@PathVariable String barcode) {
        return service.getByBarcode(barcode);
    }

    @PutMapping("/{id}")
    public Product update(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        return service.update(id, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }


    @GetMapping("/lookup/barcode/{barcode}")
    public PublicProductResponse lookupPublicBarcode(
            @PathVariable String barcode) {

        return publicProductLookupService.findByBarcode(barcode);
    }

}