package com.zawser.DATN.mapper;

import com.zawser.DATN.dto.request.UserCreationRequest;
import com.zawser.DATN.dto.request.UserUpdateRequest;
import com.zawser.DATN.dto.response.UserResponse;
import com.zawser.DATN.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
