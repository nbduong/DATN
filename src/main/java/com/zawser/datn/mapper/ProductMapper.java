package com.zawser.datn.mapper;

import java.time.LocalDateTime;
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

@Mapper(
        componentModel = "spring",
        imports = {LocalDateTime.class})
public interface ProductMapper {

    @Mapping(source = "brandId", target = "brand.id")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "discount.id", ignore = true)
    @Mapping(target = "specifications", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "prices", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Product toProduct(ProductRequest request);

    @Mapping(
            target = "salePrice",
            expression =
                    "java(product.getPrices().stream().filter(p -> Boolean.TRUE.equals(p.getIsActive())).findFirst().map(com.zawser.datn.entity.ProductPrice::getSalePrice).orElse(null))")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "specifications", expression = "java(specsToMap(product.getSpecifications()))")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "viewCount", source = "viewCount")
    @Mapping(target = "discountId", source = "discount.id")
    @Mapping(target = "discountCode", source = "discount.code")
    @Mapping(target = "discountPercent", source = "discount.discountPercent")
    @Mapping(target = "discountAmount", source = "discount.discountAmount")
    @Mapping(target = "finalPrice", ignore = true)
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "specifications", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "prices", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductRequest request);

    default Map<String, String> specsToMap(List<ProductSpecification> specs) {
        if (specs == null) {
            return null;
        }
        return specs.stream()
                .collect(Collectors.toMap(ProductSpecification::getSpecKey, ProductSpecification::getSpecValue));
    }
}
