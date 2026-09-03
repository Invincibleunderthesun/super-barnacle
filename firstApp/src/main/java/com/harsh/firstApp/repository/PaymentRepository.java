package com.harsh.firstApp.repository;

import com.harsh.firstApp.model.Order;
import com.harsh.firstApp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
