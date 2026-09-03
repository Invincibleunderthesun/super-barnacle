package com.harsh.firstApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Snapshot of a product at the time of order.
 * Decouples order history from live product data (price/name changes don't affect past orders).
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    // Snapshot fields — preserved even if the product changes
    private String productName;
    private double price;
    private int quantity;
    private Long sellerId;  // Snapshot of seller for fast seller-order queries
    private LocalDateTime createdAt;


    public OrderItem() {
        this.createdAt = LocalDateTime.now();
    }

    public OrderItem(Order order, Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.productName = product.getName();
        this.price = product.getPrice();
        this.quantity = quantity;
        this.sellerId = (product.getSeller() != null) ? product.getSeller().getId() : null;
        this.createdAt = LocalDateTime.now();
    }


    /**
     * Get subtotal for this order item (snapshot price × quantity)
     */
    public double getSubtotal() {
        return price * quantity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
}

