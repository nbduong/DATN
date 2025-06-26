package com.zawser.datn.mapper;

import com.zawser.datn.dto.request.StockInRequest;
import com.zawser.datn.dto.response.StockInResponse;
import com.zawser.datn.entity.StockIn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockInMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product.id", source = "productId")
    @Mapping(target = "inDate", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "remainingQuantity", source = "quantity")
    @Mapping(target = "createdDate", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "totalPrice", expression = "java(request.getQuantity() * request.getUnitPrice())")
    StockIn toStockIn(StockInRequest request);

    StockInResponse toStockInResponse(StockIn stockIn);
}
