package com.inventory.stock_service.service;

import com.inventory.stock_service.dto.*;
import com.inventory.stock_service.entity.StockTransaction;
import com.inventory.stock_service.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockHistoryService { // Renamed to reflect its read-only purpose

    private final StockTransactionRepository repository;
    private final ProductServiceClient productServiceClient;

    public StockHistoryService(
            StockTransactionRepository repository,
            ProductServiceClient productServiceClient,
            @Qualifier("orderWebClient") WebClient orderWebClient) {
        this.repository = repository;
        this.productServiceClient = productServiceClient;
        // Initialize OrderServiceClient (can be removed if truly never used)
    }
    public List<StockTransactionResponseDTO> getTransactionHistoryByProductId(String productId) {
        // You MUST ensure your repository has this query method defined:
        // StockTransactionRepository -> List<StockTransaction> findByProductIdOrderByTimestampDesc(String productId);

        List<StockTransaction> transactions = repository.findByProductIdOrderByTimestampDesc(productId);

        return transactions.stream()
                .map((StockTransaction t) -> StockTransactionResponseDTO.fromEntity(t)) // Assuming DTO conversion exists
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------------
    // UNCHANGED METHOD: LOW STOCK ALERTS
    // ----------------------------------------------------------------------
    public List<StockAlertDTO> generateStockAlerts() {
        List<ProductDTO> products = productServiceClient.getProductsForAlerts();

        return products.stream()
                .filter(p -> p.stockLevel() != null
                        && p.lowQuantityThreshold() != null
                        && p.stockLevel() < p.lowQuantityThreshold())
                .map(p -> new StockAlertDTO(
                        p.id(),
                        p.name(),
                        p.stockLevel(),
                        p.lowQuantityThreshold()
                ))
                .collect(Collectors.toList());
    }
}