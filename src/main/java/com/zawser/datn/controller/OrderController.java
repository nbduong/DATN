package com.zawser.datn.controller;

import java.util.List;
import java.util.UUID;

import com.zawser.datn.dto.request.ApiResponse;
import com.zawser.datn.dto.request.PlaceOrderRequest;
import com.zawser.datn.dto.request.UpdateOrderRequest;
import com.zawser.datn.dto.response.OrderResponse;
import com.zawser.datn.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ApiResponse<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest placeOrderDto) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.placeOrder(placeOrderDto))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> getAllOrders(@RequestParam(required = false) String userId) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getAllOrders(userId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderResponse> updateOrder(
            @PathVariable Long id, @RequestBody UpdateOrderRequest updateOrderRequest) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrder(id, updateOrderRequest))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.<Void>builder().build();
    }
}
