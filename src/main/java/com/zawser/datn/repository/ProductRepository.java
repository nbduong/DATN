package com.zawser.datn.repository;

import java.util.Optional;

import com.zawser.datn.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);
}
