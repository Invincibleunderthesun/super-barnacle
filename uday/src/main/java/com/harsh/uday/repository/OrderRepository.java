package com.harsh.uday.repository;

import com.harsh.uday.model.Order;
import com.harsh.uday.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    List<Order> findByUserAndStatus(User user, Order.Status status);

    List<Order> findByStatus(Order.Status status);

    // Orders between dates
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    // Count orders by status
    long countByStatus(Order.Status status);

    // User's orders sorted by date
    List<Order> findByUserOrderByOrderDateDesc(User user);

    // Recent orders
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :since ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(@Param("since") LocalDateTime since);

    // Total revenue (excluding cancelled)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    Double getTotalRevenue();

    // Orders count by user
    long countByUser(User user);

    // DB-level aggregation for order statistics
    @Query("SELECT o.status, COUNT(o), COALESCE(SUM(o.totalAmount), 0) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatsByStatus();

    // Count orders after a date
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :since")
    long countOrdersSince(@Param("since") LocalDateTime since);
}
