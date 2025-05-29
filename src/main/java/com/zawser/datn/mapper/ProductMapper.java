package com.zawser.datn.mapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.zawser.datn.dto.request.ProductRequest;
import com.zawser.datn.dto.response.ProductResponse;
import com.zawser.datn.entity.Product;
import com.zawser.datn.entity.ProductSpecification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "brandId", target = "brand.id")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "specifications", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(source = "status", target = "status")
    @Mapping(source = "viewCount", target = "viewCount")
    Product toProduct(ProductRequest request) throws IOException;

    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "specifications", expression = "java(specsToMap(product.getSpecifications()))")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "viewCount", source = "viewCount")
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "specifications", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "viewCount", source = "viewCount")
    void updateProduct(@MappingTarget Product product, ProductRequest request);

    default Map<String, String> specsToMap(List<ProductSpecification> specs) {
        return specs.stream()
                .collect(Collectors.toMap(ProductSpecification::getSpecKey, ProductSpecification::getSpecValue));
    }
}
