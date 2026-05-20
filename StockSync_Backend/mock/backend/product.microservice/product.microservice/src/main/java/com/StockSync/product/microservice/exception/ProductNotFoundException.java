package com.StockSync.product.microservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom runtime exception thrown when a requested Product is not found in the database.
 * The @ResponseStatus annotation, though optional when using a GlobalExceptionHandler, 
 * is often included for documentation and basic Spring framework mapping.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Maps this exception directly to HTTP 404
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(Long productId) {
        super("Product not found with ID: " + productId);
    }
}