package com.zawser.datn.repository;

import java.util.Optional;

import com.zawser.datn.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findById(Long id);
}
