package com.StockSync.product.microservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    PURCHASE,
    SELL;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static TransactionType fromValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return TransactionType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
