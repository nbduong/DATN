package com.zawser.datn.mapper;

import com.zawser.datn.dto.request.OrderItemRequest;
import com.zawser.datn.dto.request.PlaceOrderRequest;
import com.zawser.datn.dto.request.UpdateOrderRequest;
import com.zawser.datn.dto.response.OrderItemResponse;
import com.zawser.datn.dto.response.OrderResponse;
import com.zawser.datn.entity.Order;
import com.zawser.datn.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdDate", expression = "java(java.time.LocalDateTime.now())")
    Order toOrder(PlaceOrderRequest placeOrderRequest);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "orderItems", target = "orderItems")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "id", ignore = true)
    OrderItem toOrderItem(OrderItemRequest orderItemRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    void updateOrderFromDto(UpdateOrderRequest updateOrderRequestDto, @MappingTarget Order order);
}
