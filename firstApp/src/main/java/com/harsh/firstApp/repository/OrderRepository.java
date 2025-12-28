package com.harsh.firstApp.repository;

import com.harsh.firstApp.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
