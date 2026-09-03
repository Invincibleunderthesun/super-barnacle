package com.harsh.firstApp.repository;

import com.harsh.firstApp.model.Cart;
import com.harsh.firstApp.model.CartItem;
import com.harsh.firstApp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCartAndProduct(Cart cart, Product product);
}
