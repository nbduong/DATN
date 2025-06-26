package com.zawser.datn.repository;

import java.util.Optional;

import com.zawser.datn.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, String> {
    Optional<Discount> findByCode(String code);

    boolean existsByCode(String code);
}
