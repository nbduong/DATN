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

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "totalProfit", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Order toOrder(PlaceOrderRequest placeOrderRequest);

    @Mapping(target = "product.id", source = "productId")
    @Mapping(target = "salePrice", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "profit", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "id", ignore = true)
    OrderItem toOrderItem(OrderItemRequest orderItemRequest);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "userName")
    @Mapping(
            target = "orderItems",
            expression =
                    "java(order.getOrderItems().stream().map(this::toOrderItemResponse).collect(java.util.stream.Collectors.toList()))")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "totalProfit", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateOrderFromDto(UpdateOrderRequest updateOrderRequest, @MappingTarget Order order);
}
