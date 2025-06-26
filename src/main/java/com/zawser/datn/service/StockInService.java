package com.zawser.datn.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.zawser.datn.dto.request.StockInRequest;
import com.zawser.datn.dto.request.StockInUpdateRequest;
import com.zawser.datn.dto.response.StockInResponse;
import com.zawser.datn.entity.Product;
import com.zawser.datn.entity.StockIn;
import com.zawser.datn.mapper.StockInMapper;
import com.zawser.datn.repository.ProductRepository;
import com.zawser.datn.repository.StockInRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class StockInService {

    StockInRepository stockInRepository;
    StockInMapper stockInMapper;
    ProductRepository productRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StockInResponse createStockIn(StockInRequest request) {

        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));
        StockIn stockIn = stockInMapper.toStockIn(request);
        stockIn.setCreatedBy(name);
        stockIn.setLastModifiedBy(name);
        stockIn.setProductCode(product.getProductCode());
        stockIn.setProductName(product.getName());
        stockIn.setParcelCode(request.getParcelCode());
        // Update product quantity
        product.setQuantity(product.getQuantity() + request.getQuantity());
        productRepository.save(product);

        StockIn savedStockIn = stockInRepository.save(stockIn);
        return stockInMapper.toStockInResponse(savedStockIn);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<StockInResponse> getAllStockIns() {
        return stockInRepository.findAll().stream()
                .map(stockInMapper::toStockInResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<StockInResponse> getAvailableStockIns(String productId) {
        return stockInRepository.findByProductIdAndRemainingQuantityGreaterThanOrderByInDateAsc(productId, 0).stream()
                .map(stockInMapper::toStockInResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StockInResponse updateStockIn(String id, StockInUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        // Find existing stockIn
        StockIn stockIn = stockInRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("StockIn not found with id: " + id));

        // Get original product
        Product originalProduct = stockIn.getProduct();
        String originalProductId = originalProduct.getId();

        // Find new product
        Product newProduct = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // Handle product quantity updates
        if (!originalProductId.equals(request.getProductId())) {
            // Different product: remove quantity from original product
            originalProduct.setQuantity(originalProduct.getQuantity() - stockIn.getQuantity());
            productRepository.save(originalProduct);

            // Add new quantity to new product
            newProduct.setQuantity(newProduct.getQuantity() + request.getQuantity());
        } else {
            // Same product: calculate quantity difference
            int quantityDifference = request.getQuantity() - stockIn.getQuantity();
            newProduct.setQuantity(newProduct.getQuantity() + quantityDifference);
        }
        productRepository.save(newProduct);

        // Update stockIn fields
        stockIn.setProduct(newProduct);
        stockIn.setProductCode(newProduct.getProductCode());
        stockIn.setProductName(newProduct.getName());
        stockIn.setQuantity(request.getQuantity());
        stockIn.setUnitPrice(request.getUnitPrice());
        stockIn.setTotalPrice(request.getQuantity() * request.getUnitPrice());
        stockIn.setParcelCode(request.getParcelCode());
        stockIn.setLastModifiedBy(name);
        stockIn.setLastModifiedDate(LocalDate.now());

        // Save updated stockIn
        StockIn savedStockIn = stockInRepository.save(stockIn);
        return stockInMapper.toStockInResponse(savedStockIn);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteStockIn(String id) {
        StockIn stockIn = stockInRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("StockIn not found with id: " + id));

        // Update product quantity
        Product product = stockIn.getProduct();
        product.setQuantity(product.getQuantity() - stockIn.getQuantity());
        productRepository.save(product);

        stockInRepository.deleteById(id);
    }
}
