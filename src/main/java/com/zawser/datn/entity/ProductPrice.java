package com.zawser.datn.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product_price")
public class ProductPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @Column(nullable = false)
    Double salePrice;

    @Column(nullable = false)
    LocalDateTime startDate;

    @Column
    LocalDateTime endDate; // Có thể null nếu giá vẫn đang áp dụng

    @Column
    String priceType; // Ví dụ: "REGULAR", "PROMOTION"

    @Column
    Boolean isActive; // Trạng thái giá (true nếu giá đang áp dụng)

    @Column
    String createdBy;

    @Column
    LocalDateTime createdDate;

    @Column
    String lastModifiedBy;

    @Column
    LocalDateTime lastModifiedDate;
}
