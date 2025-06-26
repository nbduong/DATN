package com.zawser.datn.dto.response;

import java.time.LocalDateTime;

import com.zawser.datn.enums.DiscountType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiscountResponse {
    String id;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Double discountPercent;
    Double discountAmount;
    Boolean isGlobal;
    String code;
    String status;
    Integer quantity;
    String createdBy;
    String lastModifiedBy;
    LocalDateTime createdDate;
    LocalDateTime lastModifiedDate;
    DiscountType type;
}
