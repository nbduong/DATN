package com.zawser.datn.mapper;

import java.time.LocalDateTime;

import com.zawser.datn.dto.request.DiscountRequest;
import com.zawser.datn.dto.response.DiscountResponse;
import com.zawser.datn.entity.Discount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        imports = {LocalDateTime.class})
public interface DiscountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "startDate", expression = "java(LocalDateTime.parse(request.getStartDate()))")
    @Mapping(target = "endDate", expression = "java(LocalDateTime.parse(request.getEndDate()))")
    @Mapping(target = "status", defaultValue = "ACTIVE")
    Discount toDiscount(DiscountRequest request);

    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "discountPercent", source = "discountPercent")
    @Mapping(target = "discountAmount", source = "discountAmount")
    @Mapping(target = "isGlobal", source = "isGlobal")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "type", source = "type")
    DiscountResponse toDiscountResponse(Discount discount);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "startDate", expression = "java(LocalDateTime.parse(request.getStartDate()))")
    @Mapping(target = "endDate", expression = "java(LocalDateTime.parse(request.getEndDate()))")
    @Mapping(target = "status", defaultValue = "ACTIVE")
    void updateDiscount(@MappingTarget Discount discount, DiscountRequest request);

    default Discount map(String id) {
        if (id == null) {
            return null;
        }
        return Discount.builder().id(id).build();
    }
}
