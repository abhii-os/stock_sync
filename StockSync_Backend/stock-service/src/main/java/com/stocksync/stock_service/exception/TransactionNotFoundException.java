package com.stocksync.stock_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
    public class TransactionNotFoundException extends RuntimeException {
        public TransactionNotFoundException(Long id) {
            super("Stock Transaction with ID " + id + " not found.");
        }
        public TransactionNotFoundException(Long id, boolean isOrderId) {
            super("Stock Transaction linked to Order ID " + id + " not found.");
        }
    }