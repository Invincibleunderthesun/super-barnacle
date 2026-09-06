package com.harsh.uday.repository;

import com.harsh.uday.model.Cart;
import com.harsh.uday.model.CartItem;
import com.harsh.uday.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCartAndProduct(Cart cart, Product product);
}
