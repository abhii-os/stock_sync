package com.stocksync.supplier.app.service.api;

import com.stocksync.supplier.app.dto.RoleRequestDTO;
import com.stocksync.supplier.app.dto.RoleResponseDTO;
import com.stocksync.supplier.app.entity.Role;
import com.stocksync.supplier.app.enums.RoleType;

import java.util.List;

public interface RoleService {
    RoleResponseDTO createRole(RoleRequestDTO request);
    RoleResponseDTO getRoleById(Long id);
    List<RoleResponseDTO> getAllRoles();
    RoleResponseDTO updateRole(Long id, RoleRequestDTO request);
    void deleteRole(Long id);
    Role getRoleByName(RoleType roleType);
    public Role seedRoleData(RoleRequestDTO request);
}
