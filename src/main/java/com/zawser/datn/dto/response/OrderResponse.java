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
    String userName;
    String orderNumber;
    String status;
    String shippingAddress;
    String paymentMethod;
    Double totalAmount;
    Double totalProfit;
    String shipmentMethod;
    String orderNote;
    List<OrderItemResponse> orderItems;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    Boolean isDeleted;
}
