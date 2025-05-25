package com.zawser.datn.repository;

import com.zawser.datn.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"categories"})
    Optional<Product> findById(Long id);
}
