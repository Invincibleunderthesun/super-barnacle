package com.harsh.uday.repository;

import com.harsh.uday.model.Product;
import com.harsh.uday.model.Review;
import com.harsh.uday.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProduct(Product product, Pageable pageable);
    Optional<Review> findByUserAndProduct(User user, Product product);
    boolean existsByUserAndProduct(User user, Product product);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double getAverageRatingByProduct(Product product);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product")
    long countByProduct(Product product);
}
