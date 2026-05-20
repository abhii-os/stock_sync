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
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(@ModelAttribute ProductDTO productDTO) {
        MultipartFile image = productDTO.getImageFile();
        System.out.println("Image file = " +
                (image != null ? image.getOriginalFilename() + ", size: " + image.getSize() : "null"));

        try {
            Product createdProduct = productService.createProduct(productDTO);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (SupplierNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    @PutMapping(value = "/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @ModelAttribute ProductDTO productDTO) {
        MultipartFile image = productDTO.getImageFile();
        System.out.println("Update image file = " +
                (image != null ? image.getOriginalFilename() + ", size: " + image.getSize() : "null"));

        try {
            Product updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductNotFoundException | SupplierNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product: " + e.getMessage());
        }
    }

    @GetMapping("/products/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isPresent() && productOpt.get().getImageData() != null) {
            Product product = productOpt.get();
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(product.getImageType()))
                    .body(product.getImageData());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id)
                ? ResponseEntity.ok("Deleted")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PatchMapping("/products/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id,
                                         @Valid @RequestBody StockUpdateRequest request) {
        try {
            Product product = productService.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

            if (request.getTransactionType() == TransactionType.SELL &&
                    product.getCurrentStock() < request.getQuantity()) {
                return ResponseEntity.badRequest().body("Insufficient stock. Available: " +
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
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update stock: " + e.getMessage()));
        }
    }
}
