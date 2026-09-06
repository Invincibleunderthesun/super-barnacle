package com.harsh.uday.repository;

import com.harsh.uday.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Search by name (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Filter by price range (paginated)
    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // Find low stock products
    List<Product> findByStockLessThanEqual(int threshold);

    // Find by category
    List<Product> findByCategory(String category);

    // Find active products only
    List<Product> findByActiveTrue();

    // Custom query for search with multiple fields (paginated)
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    // Count by category
    long countByCategory(String category);

    // Find products with stock
    List<Product> findByStockGreaterThan(int minStock);

    // Seller-specific queries
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    long countBySellerId(Long sellerId);

    List<Product> findBySellerIdAndActiveTrue(Long sellerId);
}