package com.zawser.datn.entity;

import java.time.LocalDate;

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
public class StockIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;

    String productName;
    String productCode;

    String parcelCode;
    Integer quantity;
    Double unitPrice;
    Double totalPrice;
    LocalDate inDate;
    Integer remainingQuantity;

    String createdBy;
    LocalDate createdDate;
    String lastModifiedBy;
    LocalDate lastModifiedDate;
}
