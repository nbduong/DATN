package com.zawser.datn.mapper;

import com.zawser.datn.dto.request.UserCreationRequest;
import com.zawser.datn.dto.request.UserUpdateRequest;
import com.zawser.datn.dto.response.UserResponse;
import com.zawser.datn.entity.User;
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
