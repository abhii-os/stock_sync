package com.StockSync.product.microservice.service;

import com.StockSync.product.microservice.DTO.StockUpdatePayload;
import org.springframework.web.bind.annotation.PathVariable;

public interface OrderServiceClient {
    StockUpdatePayload getOrderDetails(@PathVariable("productId") Long productId);
}
