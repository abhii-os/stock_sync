package com.StockSync.product.microservice.DTO;

import jakarta.persistence.Version;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "SKU cannot be blank")
    private String sku;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Min(value = 0, message = "Current stock must be non-negative")
    private Integer currentStock;

    @Min(value = 0, message = "Low quantity threshold must be non-negative")
    private Integer lowQuantityThreshold;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    private boolean isActive;
    private Long categoryId;
    private Long supplierId;

    private MultipartFile imageFile;
}

