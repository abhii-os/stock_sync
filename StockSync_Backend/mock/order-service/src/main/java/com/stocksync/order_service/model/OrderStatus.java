package com.stocksync.order_service.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    FAILED;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static OrderStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}