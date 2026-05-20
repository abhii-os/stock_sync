package com.StockSync.product.microservice.service;

import com.StockSync.product.microservice.DTO.ProductDTO;
import com.StockSync.product.microservice.DTO.StockUpdatePayload;
import com.StockSync.product.microservice.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    boolean adjustProductStock(StockUpdatePayload payload);
    List<ProductDTO> getProductsForAlerts();
    Optional<Product> findById(Long id);
    Product save(Product product);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product updateProduct(Long id, Product product);
    Product updateProduct(Long id, ProductDTO productDTO);
    boolean deleteProduct(Long id);
    ProductDTO mapToDto(Product product);
    Product mapToEntity(ProductDTO productDTO);
    Product createProduct(ProductDTO productDTO);
}
