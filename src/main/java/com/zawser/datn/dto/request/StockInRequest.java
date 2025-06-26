package com.zawser.datn.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInRequest {

    @NotNull(message = "Product ID cannot be null")
    String productId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    Integer quantity;

    @NotNull(message = "Unit price cannot be null")
    @Min(value = 0, message = "Unit price must be greater than or equal to 0")
    Double unitPrice;

    String parcelCode;
}
