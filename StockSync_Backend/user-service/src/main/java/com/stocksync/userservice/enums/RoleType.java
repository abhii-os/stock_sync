package com.stocksync.userservice.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    ROLE_ADMIN("Administrator Role"),
    ROLE_MANAGER("Manager Role");



    private final String description;

    RoleType(String description) {
        this.description = description;
    }

    public String getName() {
        return this.name();
    }
}
