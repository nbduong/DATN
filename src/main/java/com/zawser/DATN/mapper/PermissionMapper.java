package com.zawser.DATN.mapper;


import com.zawser.DATN.dto.request.PermissionRequest;
import com.zawser.DATN.dto.request.UserCreationRequest;
import com.zawser.DATN.dto.request.UserUpdateRequest;
import com.zawser.DATN.dto.response.PermissionResponse;
import com.zawser.DATN.dto.response.UserResponse;
import com.zawser.DATN.entity.Permission;
import com.zawser.DATN.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);


    PermissionResponse toPermissionResponse(Permission permission);
}
