package com.zawser.datn.dto.request;

import com.zawser.datn.enums.DiscountType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiscountRequest {
    String startDate;
    String endDate;
    Double discountPercent;
    String code;
    String status;
    Integer quantity;
    Boolean isGlobal;
    DiscountType type;

    Double discountAmount;
}
