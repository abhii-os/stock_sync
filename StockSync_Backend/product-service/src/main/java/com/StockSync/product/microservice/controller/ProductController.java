package com.StockSync.product.microservice.controller;

import com.StockSync.product.microservice.DTO.ProductDTO;
import com.StockSync.product.microservice.DTO.StockUpdateRequest;
import com.StockSync.product.microservice.exception.ProductNotFoundException;
import com.StockSync.product.microservice.exception.SupplierNotFoundException;
import com.StockSync.product.microservice.model.Product;
import com.StockSync.product.microservice.model.TransactionType;
import com.StockSync.product.microservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id); // throws ProductNotFoundException
        return ResponseEntity.ok(product);
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProduct(@Valid @ModelAttribute ProductDTO productDTO) {
        Product createdProduct = productService.createProduct(productDTO); // may throw SupplierNotFoundException
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping(value = "/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @ModelAttribute ProductDTO productDTO) {
        Product updatedProduct = productService.updateProduct(id, productDTO); // may throw exceptions
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/products/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(product.getImageType()))
                .body(product.getImageData());
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id); // throws ProductNotFoundException if not found
        return ResponseEntity.ok("Deleted");
    }

    @PatchMapping("/products/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(@PathVariable Long id,
                                                           @Valid @RequestBody StockUpdateRequest request) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        if (request.getTransactionType() == TransactionType.SELL &&
                product.getCurrentStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " +
                    product.getCurrentStock() + ", Requested: " + request.getQuantity());
        }

        int newStock = switch (request.getTransactionType()) {
            case SELL -> product.getCurrentStock() - request.getQuantity();
            case PURCHASE -> product.getCurrentStock() + request.getQuantity();
        };

        product.setCurrentStock(newStock);
        product = productService.save(product);

        return ResponseEntity.ok(Map.of(
                "message", "Stock updated successfully",
                "productId", product.getId(),
                "newStockLevel", product.getCurrentStock()
        ));
    }
}