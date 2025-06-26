package com.zawser.datn.controller;

import java.util.List;

import com.zawser.datn.dto.request.ApiResponse;
import com.zawser.datn.dto.request.DiscountRequest;
import com.zawser.datn.dto.response.DiscountResponse;
import com.zawser.datn.service.DiscountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/discounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DiscountController {

    DiscountService discountService;

    @PostMapping
    public ApiResponse<DiscountResponse> createDiscount(@RequestBody DiscountRequest request) {
        return ApiResponse.<DiscountResponse>builder()
                .result(discountService.createDiscount(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<DiscountResponse> updateDiscount(@PathVariable String id, @RequestBody DiscountRequest request) {
        return ApiResponse.<DiscountResponse>builder()
                .result(discountService.updateDiscount(id, request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DiscountResponse> getDiscountById(@PathVariable String id) {
        return ApiResponse.<DiscountResponse>builder()
                .result(discountService.getDiscountById(id))
                .build();
    }

    @GetMapping("/code/{code}")
    public ApiResponse<DiscountResponse> getDiscountByCode(@PathVariable String code) {
        return ApiResponse.<DiscountResponse>builder()
                .result(discountService.getDiscountByCode(code))
                .build();
    }

    @GetMapping
    public ApiResponse<List<DiscountResponse>> getAllDiscounts() {
        return ApiResponse.<List<DiscountResponse>>builder()
                .result(discountService.getAllDiscounts())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDiscount(@PathVariable String id) {
        discountService.deleteDiscount(id);
        return ApiResponse.<Void>builder().build();
    }
}
