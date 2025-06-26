package com.zawser.datn.dto.response;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInResponse {

    String id;
    String parcelCode;
    String productName;
    String productCode;
    Integer quantity;
    Double unitPrice;
    Double totalPrice;

    String createdBy;

    LocalDate createdDate;
    String lastModifiedDate;
    String lastModifiedBy;
}
