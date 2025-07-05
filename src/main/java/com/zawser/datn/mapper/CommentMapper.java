package com.zawser.datn.mapper;

import com.zawser.datn.dto.request.CommentRequest;
import com.zawser.datn.dto.response.CommentResponse;
import com.zawser.datn.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "product.id", source = "productId")
    Comment toComment(CommentRequest request);

    CommentResponse toResponse(Comment comment);
}
