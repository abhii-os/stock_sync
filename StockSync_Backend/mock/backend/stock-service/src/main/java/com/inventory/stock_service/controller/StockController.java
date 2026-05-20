package com.inventory.stock_service.controller;

import com.inventory.stock_service.dto.StockAlertDTO;
import com.inventory.stock_service.dto.StockTransactionResponseDTO; // Assuming you still use this for fetching history
import com.inventory.stock_service.service.StockHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock") // Base path: /api/v1/stock
public class StockController {

    private final StockHistoryService service;

    public StockController(StockHistoryService service) {
        this.service = service;
    }

    // ----------------------------------------------------------------------
    // RETAINED ENDPOINT: LOW STOCK ALERTS
    // Full path: GET /api/v1/stock/alerts
    // ----------------------------------------------------------------------

    @GetMapping("/alerts")
    public ResponseEntity<List<StockAlertDTO>> getStockAlerts() {
        List<StockAlertDTO> alerts = service.generateStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/history/{productId}")
    public ResponseEntity<List<StockTransactionResponseDTO>> getHistoryByProductId(
            @PathVariable("productId") String productId) {

        // You'll need to add this method to your StockHistoryService
        List<StockTransactionResponseDTO> history = service.getTransactionHistoryByProductId(productId);
        return ResponseEntity.ok(history);
    }

}