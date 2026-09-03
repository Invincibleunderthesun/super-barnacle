package com.harsh.firstApp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    public enum Status {
        PAYMENT_PENDING,
        PENDING,
        PAID,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private double subtotal;
    private double taxRate = 0.18;  // 18% GST
    private double taxAmount;
    private double totalAmount;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PAYMENT_PENDING;

    // Shipping address snapshot (copied from Address at checkout time)
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingPincode;

    // Invoice number (generated at order time)
    @Column(unique = true)
    private String invoiceNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public Order() {}

    /**
     * Create order with GST calculation.
     * @param subtotal sum of item prices before tax
     */
    public Order(double subtotal) {
        this.subtotal = subtotal;
        this.taxRate = 0.18;
        this.taxAmount = Math.round(subtotal * taxRate * 100.0) / 100.0;
        this.totalAmount = Math.round((subtotal + taxAmount) * 100.0) / 100.0;
        this.orderDate = LocalDateTime.now();
        this.status = Status.PAYMENT_PENDING;
    }

    /**
     * Copy address data as a snapshot into this order.
     */
    public void setShippingAddressFrom(Address address) {
        this.shippingName = address.getFullName();
        this.shippingPhone = address.getPhone();
        this.shippingAddress = address.getAddressLine1()
                + (address.getAddressLine2() != null ? ", " + address.getAddressLine2() : "");
        this.shippingCity = address.getCity();
        this.shippingState = address.getState();
        this.shippingPincode = address.getPincode();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getShippingName() { return shippingName; }
    public void setShippingName(String shippingName) { this.shippingName = shippingName; }

    public String getShippingPhone() { return shippingPhone; }
    public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }

    public String getShippingState() { return shippingState; }
    public void setShippingState(String shippingState) { this.shippingState = shippingState; }

    public String getShippingPincode() { return shippingPincode; }
    public void setShippingPincode(String shippingPincode) { this.shippingPincode = shippingPincode; }

    public List<OrderStatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<OrderStatusHistory> statusHistory) { this.statusHistory = statusHistory; }
}