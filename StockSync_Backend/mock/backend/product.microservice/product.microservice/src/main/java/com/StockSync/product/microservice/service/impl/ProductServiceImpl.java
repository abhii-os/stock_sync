package com.StockSync.product.microservice.service.impl;

import com.StockSync.product.microservice.DTO.ProductDTO;
import com.StockSync.product.microservice.DTO.StockUpdatePayload;
import com.StockSync.product.microservice.Repository.CategoryRepo;
import com.StockSync.product.microservice.Repository.ProductRepo;
import com.StockSync.product.microservice.exception.CategoryNotFoundException;
import com.StockSync.product.microservice.exception.ProductNotFoundException;
import com.StockSync.product.microservice.exception.SupplierNotFoundException;
import com.StockSync.product.microservice.model.Product;
import com.StockSync.product.microservice.service.OrderServiceClient;
import com.StockSync.product.microservice.service.ProductService;
import com.StockSync.product.microservice.service.SupplierServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final OrderServiceClient orderServiceClient;
    private final SupplierServiceClient supplierServiceClient;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    public ProductServiceImpl(ProductRepo productRepo,
                              CategoryRepo categoryRepo,
                              OrderServiceClient orderServiceClient,
                              SupplierServiceClient supplierServiceClient) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.orderServiceClient = orderServiceClient;
        this.supplierServiceClient = supplierServiceClient;
    }

    private String getCurrentUsername() {
        String userId = request.getHeader("X-Auth-User-Id");
        return userId != null ? userId : "system";
    }

    @Override
    public ProductDTO mapToDto(Product product) {
        if (product == null) return null;
        return new ProductDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCurrentStock(),
                product.getLowQuantityThreshold(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getSupplierId(),
                null
        );
    }

    @Override
    public Product mapToEntity(ProductDTO productDTO) {
        if (productDTO == null) return null;

        Product product = new Product();
        product.setId(productDTO.getId());
        product.setSku(productDTO.getSku());
        product.setName(productDTO.getName());
        product.setCurrentStock(productDTO.getCurrentStock());
        product.setLowQuantityThreshold(productDTO.getLowQuantityThreshold());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setActive(productDTO.isActive());
        product.setSupplierId(productDTO.getSupplierId());

        if (productDTO.getCategoryId() != null) {
            product.setCategory(categoryRepo.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + productDTO.getCategoryId())));
        }

        return product;
    }

    @Override
    public Product createProduct(ProductDTO productDTO) {
        if (productDTO.getSupplierId() == null) {
            throw new IllegalArgumentException("Supplier ID is required");
        }

        if (!supplierServiceClient.validateSupplierExists(productDTO.getSupplierId())) {
            throw new SupplierNotFoundException("Supplier not found with ID: " + productDTO.getSupplierId());
        }

        if (productDTO.getCategoryId() != null && !categoryRepo.existsById(productDTO.getCategoryId())) {
            throw new CategoryNotFoundException("Category not found with ID: " + productDTO.getCategoryId());
        }

        Product product = mapToEntity(productDTO);
        product.setCreatedBy(getCurrentUsername());
        product.setUpdatedBy(getCurrentUsername());

        handleImageUpload(productDTO, product);

        return productRepo.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        if (productDTO.getSupplierId() != null &&
                !productDTO.getSupplierId().equals(existingProduct.getSupplierId())) {
            if (!supplierServiceClient.validateSupplierExists(productDTO.getSupplierId())) {
                throw new SupplierNotFoundException("Supplier not found with ID: " + productDTO.getSupplierId());
            }
        } else {
            productDTO.setSupplierId(existingProduct.getSupplierId());
        }

        if (productDTO.getCategoryId() != null && !categoryRepo.existsById(productDTO.getCategoryId())) {
            throw new CategoryNotFoundException("Category not found with ID: " + productDTO.getCategoryId());
        }

        if (productDTO.getSku() != null) existingProduct.setSku(productDTO.getSku());
        if (productDTO.getName() != null) existingProduct.setName(productDTO.getName());
        if (productDTO.getDescription() != null) existingProduct.setDescription(productDTO.getDescription());
        if (productDTO.getPrice() != null) existingProduct.setPrice(productDTO.getPrice());
        if (productDTO.getCurrentStock() != null) existingProduct.setCurrentStock(productDTO.getCurrentStock());
        if (productDTO.getLowQuantityThreshold() != null)
            existingProduct.setLowQuantityThreshold(productDTO.getLowQuantityThreshold());
        if (productDTO.getCategoryId() != null) {
            existingProduct.setCategory(categoryRepo.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + productDTO.getCategoryId())));
        }
        existingProduct.setSupplierId(productDTO.getSupplierId());

        if (productDTO.getImageFile() != null && !productDTO.getImageFile().isEmpty()) {
            handleImageUpload(productDTO, existingProduct);
        }

        existingProduct.setUpdatedBy(getCurrentUsername());
        return productRepo.save(existingProduct);
    }

    private void handleImageUpload(ProductDTO productDTO, Product product) {
        MultipartFile imageFile = productDTO.getImageFile();

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                byte[] imageBytes = imageFile.getBytes();
                String mimeType = imageFile.getContentType();
                if (mimeType == null || mimeType.isEmpty()) {
                    mimeType = "image/jpeg";
                }

                product.setImageData(imageBytes);
                product.setImageType(mimeType);

            } catch (IOException e) {
                throw new RuntimeException("Failed to process image file", e);
            }
        } }

    @Override
    public boolean adjustProductStock(StockUpdatePayload payload) throws ProductNotFoundException {
        StockUpdatePayload orderDetails = orderServiceClient.getOrderDetails(payload.getProductId());
        if (orderDetails == null) {
            throw new IllegalStateException("Order details not found for product ID: " + payload.getProductId());
        }

        Long productId = orderDetails.getProductId();
        Long orderId = orderDetails.getOrderId();
        int quantity = orderDetails.getQuantity();
        String type = orderDetails.getTransactionType();

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        int currentStock = product.getCurrentStock();
        int newStock;

        if ("SELL".equalsIgnoreCase(type)) {
            newStock = currentStock - quantity;
            if (newStock < 0) {
                throw new IllegalStateException("Insufficient stock for Order ID: " + orderId +
                        ", Product ID: " + productId + ". Required: " + quantity + ", Available: " + currentStock);
            }
        } else if ("PURCHASE".equalsIgnoreCase(type)) {
            newStock = currentStock + quantity;
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + type + " for Order ID: " + orderId);
        }

        product.setCurrentStock(newStock);
        product.setUpdatedBy(getCurrentUsername());
        productRepo.save(product);

        logStockUpdate(product, orderId, type, quantity, currentStock, newStock);
        return true;
    }

    private void logStockUpdate(Product product, Long orderId, String type, int quantity, int oldStock, int newStock) {
        String logMessage = String.format("Stock Update - OrderID: %d, ProductID: %d, Type: %s, Quantity: %d, Old Stock: %d, New Stock: %d",
                orderId, product.getId(), type, quantity, oldStock, newStock);
        System.out.println(logMessage);
    }

    @Override
    public List<ProductDTO> getProductsForAlerts() {
        return productRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepo.findById(id);
    }


    @Override
    public Product save(Product product) {
        return productRepo.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        return null;
    }

    @Override
    public boolean deleteProduct(Long id) {
        return productRepo.findById(id)
                .map(product -> {
                    productRepo.delete(product);
                    return true;
                })
                .orElse(false);
    }
}
