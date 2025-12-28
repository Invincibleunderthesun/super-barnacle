package com.harsh.firstApp.repository;


import com.harsh.firstApp.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
