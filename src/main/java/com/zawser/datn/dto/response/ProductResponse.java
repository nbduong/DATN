package com.zawser.datn.dto.response;

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
    Double price;
    Integer quantity;
    String brandName;
    String categoryName;
    List<String> images;
    Map<String, String> specifications;
    String status;
    Long viewCount;
    Boolean isDeleted;
}
