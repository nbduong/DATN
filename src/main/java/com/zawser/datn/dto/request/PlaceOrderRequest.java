package com.zawser.datn.dto.request;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaceOrderRequest {
    String userId;
    String status;
    String userName;
    String shippingAddress;
    String paymentMethod;
    Double totalAmount;
    String shipmentMethod;
    String orderNote;

    List<OrderItemRequest> orderItems;
}
