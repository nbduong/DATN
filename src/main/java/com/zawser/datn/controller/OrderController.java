package com.zawser.datn.controller;

import java.util.List;

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

    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrder() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getAll())
                .build();
    }

    @GetMapping("/id/{id}") // Changed from "/{id}"
    public ApiResponse<OrderResponse> getOrderById(@PathVariable String id) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(id))
                .build();
    }

    @GetMapping("/user/{userName}") // Changed from "/{userName}"
    public ApiResponse<List<OrderResponse>> getAllOrders(@PathVariable String userName) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getOrderByUserName(userName))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderResponse> updateOrder(
            @PathVariable String id, @RequestBody UpdateOrderRequest updateOrderRequest) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrder(id, updateOrderRequest))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ApiResponse.<Void>builder().build();
    }
}
