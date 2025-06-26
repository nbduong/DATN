package com.zawser.datn.dto.request;

import java.util.List;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaceOrderRequest {

    @NotBlank(message = "ID người dùng không được để trống")
    String userId;

    @NotBlank(message = "Trạng thái không được để trống")
    String status;

    @NotBlank(message = "Tên người dùng không được để trống")
    String userName;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    String shippingAddress;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    String paymentMethod;

    // Bỏ validation bắt buộc cho totalAmount vì sẽ được tính lại trong OrderService
    Double totalAmount;

    @NotBlank(message = "Phương thức vận chuyển không được để trống")
    String shipmentMethod;

    String orderNote; // Ghi chú đơn hàng, không bắt buộc

    @NotEmpty(message = "Danh sách mặt hàng không được để trống")
    @Size(min = 1, message = "Phải có ít nhất một mặt hàng")
    List<OrderItemRequest> orderItems;
}
