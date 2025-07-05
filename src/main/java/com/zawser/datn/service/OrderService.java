package com.zawser.datn.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.zawser.datn.dto.request.PlaceOrderRequest;
import com.zawser.datn.dto.request.UpdateOrderRequest;
import com.zawser.datn.dto.response.OrderResponse;
import com.zawser.datn.entity.Order;
import com.zawser.datn.entity.OrderItem;
import com.zawser.datn.entity.Product;
import com.zawser.datn.entity.StockIn;
import com.zawser.datn.entity.User;
import com.zawser.datn.mapper.OrderMapper;
import com.zawser.datn.repository.OrderRepository;
import com.zawser.datn.repository.ProductRepository;
import com.zawser.datn.repository.StockInRepository;
import com.zawser.datn.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
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
    StockInRepository stockInRepository;
    OrderMapper orderMapper;
    EmailService emailService;
    ProductService productService;

    @PreAuthorize("#userName == authentication.name or hasRole('ADMIN')")
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest placeOrder) throws MessagingException {
        User user = userRepository
                .findById(placeOrder.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        Order order = orderMapper.toOrder(placeOrder);
        order.setUser(user);
        order.setOrderNumber(System.currentTimeMillis());

        double totalAmount = 0.0;
        double totalProfit = 0.0;

        order.setOrderItems(new ArrayList<>());
        for (var itemDto : placeOrder.getOrderItems()) {
            Product product = productRepository
                    .findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + itemDto.getProductId()));

            // Calculate final price including discount
            Double finalPrice = productService.getProductFinalPrice(product.getId());

            if (product.getQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng");
            }

            List<StockIn> stockIns = stockInRepository.findByProductIdAndRemainingQuantityGreaterThanOrderByInDateAsc(
                    itemDto.getProductId(), 0);
            if (stockIns.isEmpty()) {
                throw new RuntimeException("Không có hàng tồn kho cho sản phẩm: " + product.getName());
            }

            int remainingToSell = itemDto.getQuantity();
            double totalCost = 0.0;
            List<StockIn> usedStockIns = new ArrayList<>();

            for (StockIn stockIn : stockIns) {
                if (remainingToSell <= 0) break;
                int available = stockIn.getRemainingQuantity();
                int used = Math.min(available, remainingToSell);
                totalCost += used * stockIn.getUnitPrice();
                stockIn.setRemainingQuantity(available - used);
                usedStockIns.add(stockIn);
                remainingToSell -= used;
            }

            if (remainingToSell > 0) {
                throw new RuntimeException("Không đủ hàng tồn kho cho sản phẩm: " + product.getName());
            }

            stockInRepository.saveAll(usedStockIns);
            product.setQuantity(product.getQuantity() - itemDto.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = orderMapper.toOrderItem(itemDto);
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setSalePrice(finalPrice); // Use the final price including discount
            orderItem.setUnitPrice(totalCost / itemDto.getQuantity());
            orderItem.setProfit((finalPrice - orderItem.getUnitPrice()) * itemDto.getQuantity());
            order.getOrderItems().add(orderItem);

            totalAmount += finalPrice * itemDto.getQuantity();
            totalProfit += orderItem.getProfit();

            // Update discount quantity if applicable
            if (product.getDiscount() != null
                    && "ACTIVE".equals(product.getDiscount().getStatus())) {
                product.getDiscount().setQuantity(product.getDiscount().getQuantity() - itemDto.getQuantity());
                if (product.getDiscount().getQuantity() <= 0) {
                    product.getDiscount().setStatus("INACTIVE");
                }
            }
        }

        order.setTotalAmount(totalAmount);
        order.setTotalProfit(totalProfit);
        order.setCreatedBy(placeOrder.getUserName());
        order.setLastModifiedBy(placeOrder.getUserName());
        order.setLastModifiedDate(LocalDateTime.now());
        order.setCreatedDate(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        String subject = "Order Confirmation - Order #" + savedOrder.getOrderNumber();
        String content = buildOrderEmailContent(savedOrder);
        emailService.sendHtmlEmail(user.getEmail(), subject, content);

        return orderMapper.toOrderResponse(savedOrder);
    }

    private String buildOrderEmailContent(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>");
        content.append("<html lang='vi'>");
        content.append("<head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        content.append("<title>Xác Nhận Đơn Hàng</title>");
        content.append("</head>");
        content.append(
                "<body style='margin: 0; padding: 0; font-family: Arial, Helvetica, sans-serif; line-height: 1.6; color: #333;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>");
        content.append(
                "<div style='background-color: #3b82f6; color: white; text-align: center; padding: 20px; border-radius: 8px 0px 8px 0px;'>");
        content.append(
                "<img src='https://i.postimg.cc/85W1ZLTm/logo.png' alt='Logo Công Ty' style='max-width: 150px; height: auto;'>");
        content.append("<h2 style='margin: 10px 0; font-size: 24px;'>Xác Nhận Đơn Hàng</h2>");
        content.append("</div>");
        content.append(
                "<div style='background-color: white; padding: 20px; border-radius: 5px; margin-top: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>");
        content.append("<p style='font-size: 16px;'>Kính gửi Quý khách hàng,</p>");
        content.append(
                "<p style='font-size: 14px;'>Cảm ơn Quý khách đã đặt hàng! Chúng tôi xin xác nhận đơn hàng của Quý khách đã được ghi nhận thành công. Dưới đây là chi tiết đơn hàng:</p>");
        content.append("<div style='margin-bottom: 20px;'>");
        content.append("<h3 style='font-size: 18px; color: #1e3a8a; margin-bottom: 10px;'>Chi tiết đơn hàng</h3>");
        content.append("<p style='margin: 5px 0;'><strong>Mã đơn hàng:</strong> ")
                .append(order.getOrderNumber())
                .append("</p>");
        String formattedDate = order.getCreatedDate() instanceof LocalDateTime
                ? (order.getCreatedDate()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                : order.getCreatedDate().toString();
        content.append("<p style='margin: 5px 0;'><strong>Ngày đặt hàng:</strong> ")
                .append(formattedDate)
                .append("</p>");
        content.append("</div>");
        content.append("<div style='margin-bottom: 20px;'>");
        content.append("<h3 style='font-size: 18px; color: #1e3a8a; margin-bottom: 10px;'>Sản phẩm của bạn</h3>");
        content.append("<table style='width: 100%; border-collapse: collapse; font-size: 14px;'>");
        content.append("<thead>");
        content.append("<tr style='background-color: #f3f4f6; text-align: left;'>");
        content.append("<th style='padding: 12px; border-bottom: 1px solid #ddd;'>Sản phẩm</th>");
        content.append("<th style='padding: 12px; border-bottom: 1px solid #ddd;'>Số lượng</th>");
        content.append("<th style='padding: 12px; border-bottom: 1px solid #ddd;'>Giá bán</th>");
        content.append("<th style='padding: 12px; border-bottom: 1px solid #ddd;'>Tổng</th>");
        content.append("</tr>");
        content.append("</thead>");
        content.append("<tbody>");
        for (OrderItem item : order.getOrderItems()) {
            content.append("<tr>");
            content.append("<td style='padding: 12px; border-bottom: 1px solid #ddd;'>")
                    .append(item.getProduct().getName())
                    .append("</td>");
            content.append("<td style='padding: 12px; border-bottom: 1px solid #ddd; text-align: center;'>")
                    .append(item.getQuantity())
                    .append("</td>");
            content.append("<td style='padding: 12px; border-bottom: 1px solid #ddd;'>₫")
                    .append(String.format("%,.0f", item.getSalePrice()))
                    .append("</td>");
            content.append("<td style='padding: 12px; border-bottom: 1px solid #ddd;'>₫")
                    .append(String.format("%,.0f", item.getSalePrice() * item.getQuantity()))
                    .append("</td>");
        }
        content.append("</tbody>");
        content.append("</table>");
        content.append("</div>");
        content.append("<p style='font-size: 16px; font-weight: bold; margin: 10px 0; text-align: right;'>Tổng tiền: ₫")
                .append(String.format("%,.0f", order.getTotalAmount()))
                .append("</p>");
        content.append("<p style='text-align: center;'>");
        content.append(
                "<a href='http://localhost:5173/user/order' style='display: inline-block; padding: 10px 20px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 5px; font-size: 14px;'>Xem đơn hàng của bạn</a>");
        content.append("</p>");
        content.append(
                "<p style='font-size: 14px;'>Chúng tôi sẽ thông báo khi đơn hàng của Quý khách được giao. Nếu có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua <a href='mailto:nbduong1905@gmail.com' style='color: #3b82f6;'>nbduong1905@gmail.com</a>.</p>");
        content.append("</div>");
        content.append("<div style='text-align: center; font-size: 12px; color: #777; margin-top: 20px;'>");
        content.append("<p>© 2025 Công ty của bạn. Đã đăng ký bản quyền.</p>");
        content.append(
                "<p><a href='http://localhost:5173' style='color: #3b82f6; text-decoration: none;'>Truy cập trang web của chúng tôi</a> | <a href='mailto:nbduong1905@gmail.com' style='color: #3b82f6; text-decoration: none;'>Liên hệ</a></p>");
        content.append("</div>");
        content.append("</div>");
        content.append("</body>");
        content.append("</html>");
        return content.toString();
    }

    @PostAuthorize("returnObject.userName == authentication.name or hasRole('ADMIN')")
    public OrderResponse getOrderById(String id) {
        Order order =
                orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + id));
        return orderMapper.toOrderResponse(order);
    }

    @PreAuthorize("#userName == authentication.name or hasRole('ADMIN')")
    public List<OrderResponse> getOrderByUserName(String userName) {
        List<Order> orders = orderRepository.findByUserName(userName);
        return orders.stream().map(orderMapper::toOrderResponse).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAll() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(orderMapper::toOrderResponse).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Transactional
    public OrderResponse updateOrder(String id, UpdateOrderRequest updateOrderRequest) throws MessagingException {
        Order order =
                orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + id));
        User user = userRepository
                .findByUsername(updateOrderRequest.getUserName())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Khôi phục số lượng sản phẩm cũ
        for (OrderItem existingItem : order.getOrderItems()) {
            Product product = existingItem.getProduct();
            product.setQuantity(product.getQuantity() + existingItem.getQuantity());
            List<StockIn> stockIns = stockInRepository.findByProductIdAndRemainingQuantityGreaterThanOrderByInDateAsc(
                    product.getId(), 0);
            int remainingToRestore = existingItem.getQuantity();
            for (StockIn stockIn : stockIns) {
                if (remainingToRestore <= 0) break;
                int available = stockIn.getRemainingQuantity();
                int restore = Math.min(available, remainingToRestore);
                stockIn.setRemainingQuantity(available + restore);
                remainingToRestore -= restore;
            }
            stockInRepository.saveAll(stockIns);
            productRepository.save(product);
        }

        // Xóa các order items cũ
        order.getOrderItems().clear();

        // Thêm các order items mới
        double totalAmount = 0.0;
        double totalProfit = 0.0;
        if (updateOrderRequest.getOrderItems() != null) {
            for (var itemDto : updateOrderRequest.getOrderItems()) {
                Product product = productRepository
                        .findById(itemDto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + itemDto.getProductId()));

                // Calculate final price including discount
                Double finalPrice = productService.getProductFinalPrice(product.getId());

                if (product.getQuantity() < itemDto.getQuantity()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng");
                }

                List<StockIn> stockIns =
                        stockInRepository.findByProductIdAndRemainingQuantityGreaterThanOrderByInDateAsc(
                                itemDto.getProductId(), 0);
                if (stockIns.isEmpty()) {
                    throw new RuntimeException("Không có hàng tồn kho cho sản phẩm: " + product.getName());
                }

                int remainingToSell = itemDto.getQuantity();
                double totalCost = 0.0;
                List<StockIn> usedStockIns = new ArrayList<>();

                for (StockIn stockIn : stockIns) {
                    if (remainingToSell <= 0) break;
                    int available = stockIn.getRemainingQuantity();
                    int used = Math.min(available, remainingToSell);
                    totalCost += used * stockIn.getUnitPrice();
                    stockIn.setRemainingQuantity(available - used);
                    usedStockIns.add(stockIn);
                    remainingToSell -= used;
                }

                if (remainingToSell > 0) {
                    throw new RuntimeException("Không đủ hàng tồn kho cho sản phẩm: " + product.getName());
                }

                stockInRepository.saveAll(usedStockIns);
                product.setQuantity(product.getQuantity() - itemDto.getQuantity());
                productRepository.save(product);

                OrderItem orderItem = orderMapper.toOrderItem(itemDto);
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setSalePrice(finalPrice); // Use the final price including discount
                orderItem.setUnitPrice(totalCost / itemDto.getQuantity());
                orderItem.setProfit((finalPrice - orderItem.getUnitPrice()) * itemDto.getQuantity());
                order.getOrderItems().add(orderItem);

                totalAmount += finalPrice * itemDto.getQuantity();
                totalProfit += orderItem.getProfit();

                // Update discount quantity if applicable
                if (product.getDiscount() != null
                        && "ACTIVE".equals(product.getDiscount().getStatus())) {
                    product.getDiscount().setQuantity(product.getDiscount().getQuantity() - itemDto.getQuantity());
                    if (product.getDiscount().getQuantity() <= 0) {
                        product.getDiscount().setStatus("INACTIVE");
                    }
                }
            }
        }

        orderMapper.updateOrderFromDto(updateOrderRequest, order);
        order.setTotalAmount(totalAmount);
        order.setTotalProfit(totalProfit);
        order.setUserName(updateOrderRequest.getUserName());
        order.setLastModifiedBy(updateOrderRequest.getUserName());
        order.setLastModifiedDate(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        if (Objects.equals(updatedOrder.getStatus(), "Shipped")) {
            String subject = "Order Confirmation - Order #" + updatedOrder.getOrderNumber();
            String content = buildDeliverySuccessEmailContent(updatedOrder);
            emailService.sendHtmlEmail(user.getEmail(), subject, content);
        }

        return orderMapper.toOrderResponse(updatedOrder);
    }

    private String buildDeliverySuccessEmailContent(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>");
        content.append("<html lang='vi'>");
        content.append("<head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        content.append("<title>Đơn Hàng Đã Giao Thành Công</title>");
        content.append("</head>");
        content.append(
                "<body style='margin: 0; padding: 0; font-family: Arial, Helvetica, sans-serif; line-height: 1.6; color: #333;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>");
        content.append(
                "<div style='background-color: #3b82f6; color: white; text-align: center; padding: 20px; border-radius: 8px 0px 8px 0px;'>");
        content.append(
                "<img src='https://i.postimg.cc/85W1ZLTm/logo.png' alt='Logo Công Ty' style='max-width: 150px; height: auto;'>");
        content.append("<h2 style='margin: 10px 0; font-size: 24px;'>Đơn Hàng Đã Giao Thành Công</h2>");
        content.append("</div>");
        content.append(
                "<div style='background-color: white; padding: 20px; border-radius: 5px; margin-top: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>");
        content.append("<p style='font-size: 16px;'>Kính gửi Quý khách hàng,</p>");
        content.append(
                "<p style='font-size: 14px;'>Chúng tôi xin thông báo rằng đơn hàng của Quý khách đã được giao thành công. Cảm ơn Quý khách đã tin tưởng và ủng hộ chúng tôi!</p>");
        content.append("<div style='margin-bottom: 20px;'>");
        content.append("<h3 style='font-size: 18px; color: #1e3a8a; margin-bottom: 10px;'>Chi tiết đơn hàng</h3>");
        content.append("<p style='margin: 5px 0;'><strong>Mã đơn hàng:</strong> ")
                .append(order.getOrderNumber())
                .append("</p>");
        content.append("<div style='margin-bottom: 20px;'>");
        content.append("<h3 style='font-size: 18px; color: #1e3a8a; margin-bottom: 10px;'>Sản phẩm của bạn</h3>");
        content.append("<table style='width: 100%; border-collapse: collapse; font-size: 14px;'>");
        content.append("<thead>");
        content.append("<tr style='background-color: #f3f4f6; text-align: left;'>");
        content.append("<th style='padding: 12px; border-bottom: 1px solid #ddd;'>Sản phẩm</th>");
        content.append("<th style='padding: 12px; border-bottom: 1px solid #ddd;'>Số lượng</th>");
        content.append("<th style='padding: 12px; border-bottom: 1px solid #ddd;'>Giá bán</th>");
        content.append("<th style='padding: 12px; border-bottom: 1px solid #ddd;'>Tổng tiền</th>");
        content.append("</tr>");
        content.append("</thead>");
        content.append("<tbody>");
        for (OrderItem item : order.getOrderItems()) {
            content.append("<tr>");
            content.append("<td style='padding: 12px; border-bottom: 1px solid #ddd;'>")
                    .append(item.getProduct().getName())
                    .append("</td>");
            content.append("<td style='padding: 12px; border-bottom: 1px solid #ddd; text-align: center;'>")
                    .append(item.getQuantity())
                    .append("</td>");
            content.append("<td style='padding: 12px; border-bottom: 1px solid #ddd; text-align: center;'>")
                    .append(item.getSalePrice())
                    .append("</td>");
            content.append("<td style='padding: 12px; border-bottom: 1px solid #ddd;'>₫")
                    .append(String.format("%,.0f", item.getSalePrice() * item.getQuantity()))
                    .append("</td>");
            content.append("</tr>");
        }
        content.append("</tbody>");
        content.append("</table>");
        content.append("</div>");
        content.append("<p style='font-size: 16px; font-weight: bold; margin: 10px 0; text-align: right;'>Tổng tiền: ₫")
                .append(String.format("%,.0f", order.getTotalAmount()))
                .append("</p>");
        content.append("<p style='text-align: center;'>");
        content.append(
                "<a href='http://yourdomain.com/reviews' style='display: inline-block; padding: 10px 20px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 5px; font-size: 14px;'>Đánh Giá Sản Phẩm</a>");
        content.append("</p>");
        content.append(
                "<p style='font-size: 14px;'>Chúng tôi mong nhận được ý kiến đánh giá của Quý khách về sản phẩm và dịch vụ. Nếu có bất kỳ câu hỏi hoặc cần hỗ trợ, vui lòng liên hệ qua <a href='mailto:support@yourcompany.com' style='color: #3b82f6;'>support@yourcompany.com</a>.</p>");
        content.append("</div>");
        content.append("<div style='text-align: center; font-size: 12px; color: #777; margin-top: 20px;'>");
        content.append("<p>© 2025 Công ty của bạn. Đã đăng ký bản quyền.</p>");
        content.append(
                "<p><a href='http://yourdomain.com' style='color: #3b82f6; text-decoration: none;'>Truy cập trang web của chúng tôi</a> | <a href='mailto:support@yourcompany.com' style='color: #3b82f6; text-decoration: none;'>Liên hệ</a></p>");
        content.append("</div>");
        content.append("</div>");
        content.append("</body>");
        content.append("</html>");
        return content.toString();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Transactional
    public void deleteOrder(String id) {
        Order order =
                orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + id));
        // Khôi phục số lượng tồn kho
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            List<StockIn> stockIns = stockInRepository.findByProductIdAndRemainingQuantityGreaterThanOrderByInDateAsc(
                    product.getId(), 0);
            int remainingToRestore = item.getQuantity();
            for (StockIn stockIn : stockIns) {
                if (remainingToRestore <= 0) break;
                int available = stockIn.getRemainingQuantity();
                int restore = Math.min(available, remainingToRestore);
                stockIn.setRemainingQuantity(available + restore);
                remainingToRestore -= restore;
            }
            stockInRepository.saveAll(stockIns);
            productRepository.save(product);
        }
        order.setIsDeleted(true);
        orderRepository.save(order);
    }
}
