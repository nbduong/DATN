package com.zawser.datn.entity;

import java.time.LocalDateTime;

import com.zawser.datn.enums.DiscountType;
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
public class Discount {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    String id;

    @Column(columnDefinition = "TIMESTAMP")
    LocalDateTime startDate;

    @Column(columnDefinition = "TIMESTAMP")
    LocalDateTime endDate;

    @Enumerated(EnumType.STRING) // Add this annotation
    DiscountType type;

    Double discountAmount;
    Double discountPercent;
    String code;
    String status;
    Integer quantity;
    String createdBy;
    String lastModifiedBy;
    Boolean isGlobal;

    @Builder.Default
    LocalDateTime createdDate = LocalDateTime.now();

    LocalDateTime lastModifiedDate;
}
