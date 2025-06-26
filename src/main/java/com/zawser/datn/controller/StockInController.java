package com.zawser.datn.controller;

import java.util.List;

import com.zawser.datn.dto.request.ApiResponse;
import com.zawser.datn.dto.request.StockInRequest;
import com.zawser.datn.dto.request.StockInUpdateRequest;
import com.zawser.datn.dto.response.StockInResponse;
import com.zawser.datn.service.StockInService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/stock-in")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockInController {

    StockInService stockInService;

    @PostMapping
    public ApiResponse<StockInResponse> createStockIn(@RequestBody StockInRequest request) {
        return ApiResponse.<StockInResponse>builder()
                .result(stockInService.createStockIn(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<StockInResponse>> getAllStockIns() {
        return ApiResponse.<List<StockInResponse>>builder()
                .result(stockInService.getAllStockIns())
                .build();
    }

    @GetMapping("/available/{productId}")
    public ApiResponse<List<StockInResponse>> getAvailableStockIns(@PathVariable String productId) {
        return ApiResponse.<List<StockInResponse>>builder()
                .result(stockInService.getAvailableStockIns(productId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<StockInResponse> updateStockIn(
            @PathVariable String id, @Valid @RequestBody StockInUpdateRequest request) {
        return ApiResponse.<StockInResponse>builder()
                .result(stockInService.updateStockIn(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStockIn(@PathVariable String id) {
        stockInService.deleteStockIn(id);
        return ApiResponse.<Void>builder().build();
    }
}
