package com.stocksync.supplier.order_service.exception;

import java.math.BigInteger;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(BigInteger id) {
        super("Transaction not found with ID: " + id);
    }
}