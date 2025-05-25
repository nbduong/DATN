package com.zawser.datn.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
