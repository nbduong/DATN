package com.zawser.datn.repository;

import java.util.Optional;

import com.zawser.datn.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findById(String id);
}
