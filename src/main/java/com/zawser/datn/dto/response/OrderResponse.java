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
     Long id;
     String userId;
     Double totalAmount;
     String status;
     LocalDateTime createdDate;
     List<OrderItemResponse> orderItems;
}
