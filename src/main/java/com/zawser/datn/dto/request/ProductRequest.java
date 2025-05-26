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

    List<Long> categories;

    List<MultipartFile> images;
}
