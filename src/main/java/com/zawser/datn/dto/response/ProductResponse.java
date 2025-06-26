package com.zawser.datn.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

    String id;
    String name;
    String productCode;
    String description;
    Double salePrice; // Giá bán hiện tại (từ ProductPrice isActive = true)
    Integer quantity;
    String discountId;
    Double discountPercent;
    Double discountAmount;
    String discountCode;
    Double finalPrice;
    String categoryId;
    String categoryName;
    String brandId;
    String brandName;
    List<String> images;
    Map<String, String> specifications;
    String status;
    Long viewCount;
    Boolean isDeleted;
    String createdBy;
    LocalDate createdDate;
    String lastModifiedBy;
    LocalDate lastModifiedDate;
}
