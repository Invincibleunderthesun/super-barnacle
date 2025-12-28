package com.harsh.firstApp.repository;

import com.harsh.firstApp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}