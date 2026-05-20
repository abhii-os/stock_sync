package com.StockSync.product.microservice.DTO;

import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;
    private String sku;
    private String name;

    // --- ADDED STOCK FIELDS ---
    private Integer currentStock;
    private Integer lowQuantityThreshold;
    // --------------------------

    private String description;
    private BigDecimal price;
    private boolean isActive;
    private Long categoryId;
    private Long supplierId;
    private MultipartFile imageFile; // Add this field
    // Note: The @Version annotation belongs on the entity field, not the DTO class.
    // The Lombok annotations will automatically regenerate the constructor
    // and getters/setters to include the new fields.
}