package com.zawser.datn.repository;

import java.util.List;

import com.zawser.datn.entity.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockInRepository extends JpaRepository<StockIn, String> {
    List<StockIn> findByProductIdAndRemainingQuantityGreaterThanOrderByInDateAsc(
            String productId, int remainingQuantity);
}
