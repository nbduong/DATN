package com.zawser.datn.mapper;

import com.zawser.datn.dto.request.RoleRequest;
import com.zawser.datn.dto.response.RoleResponse;
import com.zawser.datn.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
