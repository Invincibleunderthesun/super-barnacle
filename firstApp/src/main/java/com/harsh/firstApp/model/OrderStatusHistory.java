package com.harsh.firstApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status; // PENDING, SHIPPED, DELIVERED, CANCELLED
    private LocalDateTime changedAt;
    private String changedBy; // email of user who made the change

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    public OrderStatusHistory() {
    }

    public OrderStatusHistory(String status, Order order) {
        this.status = status;
        this.order = order;
        this.changedAt = LocalDateTime.now();
    }

    public OrderStatusHistory(String status, Order order, String changedBy) {
        this.status = status;
        this.order = order;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}