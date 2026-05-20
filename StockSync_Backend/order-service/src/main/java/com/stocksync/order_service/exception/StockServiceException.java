package com.stocksync.order_service.exception;

public class StockServiceException extends RuntimeException {
    public StockServiceException(String message) {
        super("Invalid Transaction Completion " + message);
    }
}