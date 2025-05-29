package com.zawser.datn.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.zawser.datn.dto.request.PlaceOrderRequest;
import com.zawser.datn.dto.request.UpdateOrderRequest;
import com.zawser.datn.dto.response.OrderResponse;
import com.zawser.datn.entity.Order;
import com.zawser.datn.entity.OrderItem;
import com.zawser.datn.entity.Product;
import com.zawser.datn.entity.User;
import com.zawser.datn.mapper.OrderMapper;
import com.zawser.datn.repository.OrderRepository;
import com.zawser.datn.repository.ProductRepository;
import com.zawser.datn.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

    OrderRepository orderRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    OrderMapper orderMapper;

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest placeOrder) {
        // Kiểm tra user hợp lệ
        User user = userRepository
                .findById(placeOrder.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Map DTO to Order entity
        Order order = orderMapper.toOrder(placeOrder);
        order.setUser(user);
        double totalAmount = 0.0;

        // Xử lý các mục trong đơn hàng
        for (var itemDto : placeOrder.getOrderItems()) {
            Product product = productRepository
                    .findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + itemDto.getProductId()));

            // Kiểm tra số lượng tồn kho
            if (product.getQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng");
            }

            // Map OrderItemRequestDto to OrderItem
            OrderItem orderItem = orderMapper.toOrderItem(itemDto);
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setPrice(product.getPrice());
            order.getOrderItems().add(orderItem);

            // Cập nhật tổng giá
            totalAmount += product.getPrice() * itemDto.getQuantity();

            // Cập nhật số lượng sản phẩm trong kho
            product.setQuantity(product.getQuantity() - itemDto.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Map Order entity to OrderResponseDto
        return orderMapper.toOrderResponse(savedOrder);
    }
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public OrderResponse getOrderById(Long id) {
        Order order =
                orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + id));
        return orderMapper.toOrderResponse(order);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAllOrders(String userId) {
        List<Order> orders = userId != null ? orderRepository.findByUser_Id(userId) : orderRepository.findAll();
        return orders.stream().map(orderMapper::toOrderResponse).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Transactional
    public OrderResponse updateOrder(Long id, UpdateOrderRequest updateOrderRequest) {
        Order order =
                orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + id));

        // Map DTO to existing Order entity
        orderMapper.updateOrderFromDto(updateOrderRequest, order);

        // Xử lý cập nhật order items
        if (updateOrderRequest.getOrderItems() != null) {
            // Khôi phục số lượng sản phẩm cũ
            for (OrderItem existingItem : order.getOrderItems()) {
                Product product = existingItem.getProduct();
                product.setQuantity(product.getQuantity() + existingItem.getQuantity());
                productRepository.save(product);
            }

            // Xóa các order items cũ
            order.getOrderItems().clear();

            // Thêm các order items mới
            double totalAmount = 0.0;
            for (var itemDto : updateOrderRequest.getOrderItems()) {
                Product product = productRepository
                        .findById(itemDto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + itemDto.getProductId()));

                if (product.getQuantity() < itemDto.getQuantity()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng");
                }

                OrderItem orderItem = orderMapper.toOrderItem(itemDto);
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setPrice(product.getPrice());
                order.getOrderItems().add(orderItem);

                totalAmount += product.getPrice() * itemDto.getQuantity();
                product.setQuantity(product.getQuantity() - itemDto.getQuantity());
                productRepository.save(product);
            }
            order.setTotalAmount(totalAmount);
        }

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Transactional
    public void deleteOrder(Long id) {
        Order order =
                orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + id));

        // Khôi phục số lượng sản phẩm trong kho
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        orderRepository.delete(order);
    }
}
