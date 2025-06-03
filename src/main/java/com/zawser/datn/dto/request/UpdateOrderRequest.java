package com.zawser.datn.dto.request;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrderRequest {
    String userName;
    String status;
    String shippingAddress;
    String paymentMethod;
    String shipmentMethod;
    String orderNote;
    private List<OrderItemRequest> orderItems;
}
