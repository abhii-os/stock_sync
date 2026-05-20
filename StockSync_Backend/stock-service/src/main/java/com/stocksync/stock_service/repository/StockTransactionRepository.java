package com.stocksync.stock_service.repository;

import com.stocksync.stock_service.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    // Spring Data JPA automatically implements this method.
    List<StockTransaction> findByProductIdOrderByTimestampDesc(String productId);
}