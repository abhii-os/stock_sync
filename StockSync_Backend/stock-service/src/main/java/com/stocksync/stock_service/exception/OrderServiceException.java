package com.stocksync.stock_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public class OrderServiceException extends RuntimeException {
        public OrderServiceException(String message) {
            super("Order Service error: " + message);
        }
    }