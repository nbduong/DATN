package com.zawser.datn.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    @NotBlank(message = "Name is required")
    String name;

    @Nullable
    String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    Double price;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Integer quantity;

    @NotBlank(message = "Category is required")
    Long categoryId;

    @NotBlank(message = "Brand is required")
    Long brandId;

    String productCode;

    String status;

    @Nullable
    List<MultipartFile> images;

    String specificationsJson;

    @Nullable
    Long viewCount;

}
