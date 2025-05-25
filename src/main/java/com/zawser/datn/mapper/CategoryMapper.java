package com.zawser.datn.mapper;

import com.zawser.datn.dto.request.CategoryRequest;
import com.zawser.datn.dto.response.CategoryResponse;
import com.zawser.datn.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryRequest category);

    CategoryResponse toCategoryResponse(Category category);
}
