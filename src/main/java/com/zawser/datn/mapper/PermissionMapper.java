package com.zawser.datn.mapper;

import com.zawser.datn.dto.request.PermissionRequest;
import com.zawser.datn.dto.response.PermissionResponse;
import com.zawser.datn.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
