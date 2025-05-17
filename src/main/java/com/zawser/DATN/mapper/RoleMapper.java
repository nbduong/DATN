package com.zawser.DATN.mapper;

import com.zawser.DATN.dto.request.RoleRequest;
import com.zawser.DATN.dto.response.RoleResponse;
import com.zawser.DATN.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
