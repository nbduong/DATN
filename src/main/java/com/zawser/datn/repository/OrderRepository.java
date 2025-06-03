package com.zawser.datn.repository;

import java.util.List;

import com.zawser.datn.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserName(String userName);
}
