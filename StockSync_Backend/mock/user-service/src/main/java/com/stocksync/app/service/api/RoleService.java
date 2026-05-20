package com.stocksync.userservice.service.api;

import com.stocksync.userservice.dto.RoleRequestDTO;
import com.stocksync.userservice.dto.RoleResponseDTO;
import com.stocksync.userservice.entity.Role;
import com.stocksync.userservice.enums.RoleType;

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
