package com.zawser.datn.service;

import java.util.List;

import com.zawser.datn.dto.request.BrandRequest;
import com.zawser.datn.dto.response.BrandResponse;
import com.zawser.datn.entity.Brand;
import com.zawser.datn.mapper.BrandMapper;
import com.zawser.datn.repository.BrandRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandService {

    BrandRepository brandRepository;
    BrandMapper brandMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse createBrand(BrandRequest request) {
        Brand brand = brandMapper.toBrand(request);
        brand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(brand);
    }

    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toBrandResponse)
                .toList();
    }

    public BrandResponse getBrandById(Long id) {
        return brandMapper.toBrandResponse(
                brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Brand not found")));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }
}
