package com.zawser.datn.dto.response;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private List<CategoryResponse> categories;
    private List<String> images;
}
