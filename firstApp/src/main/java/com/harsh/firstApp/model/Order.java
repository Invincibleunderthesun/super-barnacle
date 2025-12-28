package com.harsh.firstApp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders") // 'order' is reserved in SQL, so use 'orders'
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    @ManyToMany
    private List<Product> products;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private double totalAmount;

    public Order() {}

    public Order(List<Product> products, double totalAmount) {
        this.products = products;
        this.totalAmount = totalAmount;
        this.orderDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}