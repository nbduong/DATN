package com.zawser.datn.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import com.zawser.datn.dto.request.DiscountRequest;
import com.zawser.datn.dto.response.DiscountResponse;
import com.zawser.datn.entity.Discount;
import com.zawser.datn.mapper.DiscountMapper;
import com.zawser.datn.repository.DiscountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountMapper discountMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public DiscountResponse createDiscount(DiscountRequest request) {
        validateDiscountRequest(request);

        if (discountRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Discount code already exists");
        }

        Discount discount = discountMapper.toDiscount(request);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        discount.setCreatedBy(username);
        discount.setLastModifiedBy(username);
        discount.setCreatedDate(LocalDateTime.now());
        discount.setIsGlobal(request.getIsGlobal() != null && request.getIsGlobal());

        return discountMapper.toDiscountResponse(discountRepository.save(discount));
    }

    private void validateDiscountRequest(DiscountRequest request) {
        if (request.getType() == null) {
            throw new IllegalArgumentException("Discount type is required");
        }

        switch (request.getType()) {
            case PERCENTAGE -> {
                if (request.getDiscountPercent() == null
                        || request.getDiscountPercent() < 0
                        || request.getDiscountPercent() > 100) {
                    throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
                }
            }
            case FIXED_AMOUNT -> {
                if (request.getDiscountAmount() == null || request.getDiscountAmount() < 0) {
                    throw new IllegalArgumentException("Fixed amount discount must be greater than 0");
                }
            }
        }
        // Validate dates
        try {
            LocalDateTime startDate = LocalDateTime.parse(request.getStartDate());
            LocalDateTime endDate = LocalDateTime.parse(request.getEndDate());

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date");
            }

            if (startDate.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Start date cannot be in the past");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public DiscountResponse updateDiscount(String id, DiscountRequest request) {
//        validateDiscountRequest(request);

        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discount not found"));

        // Kiểm tra xem startDate đã qua chưa
        boolean isStartDatePassed = discount.getStartDate() != null && discount.getStartDate().isBefore(LocalDateTime.now());

        // Ánh xạ các trường từ request
        discountMapper.updateDiscount(discount, request);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        discount.setLastModifiedBy(username);
        discount.setLastModifiedDate(LocalDateTime.now());
        discount.setIsGlobal(request.getIsGlobal() != null ? request.getIsGlobal() : false);

        // Chỉ cập nhật startDate nếu nó được cung cấp và ngày bắt đầu chưa qua
        if (!isStartDatePassed && request.getStartDate() != null) {
            try {
                discount.setStartDate(LocalDateTime.parse(request.getStartDate().toString()));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
            }
        }

        // Lưu entity
        Discount updatedDiscount = discountRepository.save(discount);

        // Chuyển đổi sang DiscountResponse
        return discountMapper.toDiscountResponse(updatedDiscount);
    }

    public DiscountResponse getDiscountById(String id) {
        return discountMapper.toDiscountResponse(
                discountRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Discount not found")));
    }

    public DiscountResponse getDiscountByCode(String code) {
        return discountMapper.toDiscountResponse(discountRepository
                .findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Discount not found")));
    }

    public List<DiscountResponse> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(discountMapper::toDiscountResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteDiscount(String id) {
        if (!discountRepository.existsById(id)) {
            throw new EntityNotFoundException("Discount not found");
        }
        discountRepository.deleteById(id);
    }
}
