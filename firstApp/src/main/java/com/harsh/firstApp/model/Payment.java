package com.harsh.firstApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Payment record linked to an Order.
 * Tracks Razorpay payment lifecycle (created → paid/failed → refunded).
 */
@Entity
@Table(name = "payments")
public class Payment {

    public enum PaymentStatus {
        CREATED,
        PAID,
        FAILED,
        REFUNDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(unique = true)
    private String razorpayOrderId;

    private String razorpayPaymentId;
    private String razorpaySignature;

    private double amount;
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.CREATED;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    // For refunds
    private String refundId;
    private LocalDateTime refundedAt;

    public Payment() {
        this.createdAt = LocalDateTime.now();
    }

    public Payment(Order order, String razorpayOrderId, double amount) {
        this.order = order;
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
}
