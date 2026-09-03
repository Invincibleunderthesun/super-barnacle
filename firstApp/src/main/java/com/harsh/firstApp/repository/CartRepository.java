package com.harsh.firstApp.repository;

import com.harsh.firstApp.model.Cart;
import com.harsh.firstApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
