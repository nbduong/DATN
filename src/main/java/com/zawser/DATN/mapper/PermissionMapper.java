package com.zawser.DATN.mapper;

import com.zawser.DATN.dto.request.PermissionRequest;
import com.zawser.DATN.dto.response.PermissionResponse;
import com.zawser.DATN.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
