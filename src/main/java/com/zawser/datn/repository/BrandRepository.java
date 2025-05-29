package com.zawser.datn.repository;

import com.zawser.datn.entity.Brand;
import com.zawser.datn.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findById(Long id);

}
