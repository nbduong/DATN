package com.zawser.datn.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    Long orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    String userName;

    String orderNote;

    @Column(nullable = false)
    Double totalAmount;

    @Column(nullable = false)
    String shippingAddress;

    @Column(nullable = false)
    String paymentMethod;

    @Column(nullable = false)
    String shipmentMethod;

    @Column(nullable = false)
    String status;

    Double totalProfit;

    @Builder.Default
    LocalDateTime createdDate = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItem> orderItems = new ArrayList<>();

    Boolean isDeleted;

    String createdBy;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
}
