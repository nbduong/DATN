package com.zawser.datn.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    String id;
    String userId;
    String orderNumber;
    String userName;
    Double totalAmount;
    String shippingAddress;
    String paymentMethod;
    String shipmentMethod;
    String orderNote;
    String status;
    LocalDateTime createdDate;
    List<OrderItemResponse> orderItems;

    Boolean isDeleted;
}
