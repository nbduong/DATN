package com.zawser.datn.mapper;

import com.zawser.datn.dto.request.ProductRequest;
import com.zawser.datn.dto.response.CategoryResponse;
import com.zawser.datn.dto.response.ProductResponse;
import com.zawser.datn.entity.Category;
import com.zawser.datn.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.io.IOException;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "categories", ignore = true )
    Product toProduct(ProductRequest request) throws IOException;
    ProductResponse toProductResponse(Product product);
    CategoryResponse toCategoryResponse(Category category);
}

