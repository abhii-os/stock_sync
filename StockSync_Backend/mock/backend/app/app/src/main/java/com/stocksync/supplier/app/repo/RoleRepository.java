package com.stocksync.supplier.app.repo;

import com.stocksync.supplier.app.entity.Role;
import com.stocksync.supplier.app.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
