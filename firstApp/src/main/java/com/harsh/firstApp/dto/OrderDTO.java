package com.harsh.firstApp.dto;

import com.harsh.firstApp.model.Order;
import com.harsh.firstApp.model.OrderItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Flattened order DTO — no JPA proxies, safe for JSON serialization.
 */
public class OrderDTO {
    private Long id;
    private String invoiceNumber;
    private String status;
    private double subtotal;
    private double taxAmount;
    private double totalAmount;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
    private UserDTO user;

    public static OrderDTO from(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.id = order.getId();
        dto.invoiceNumber = order.getInvoiceNumber();
        dto.status = order.getStatus().name();
        dto.subtotal = order.getSubtotal();
        dto.taxAmount = order.getTaxAmount();
        dto.totalAmount = order.getTotalAmount();
        dto.shippingAddress = order.getShippingAddress();
        dto.createdAt = order.getOrderDate();
        if (order.getItems() != null) {
            dto.items = order.getItems().stream().map(OrderItemDTO::from).collect(Collectors.toList());
        }
        if (order.getUser() != null) {
            dto.user = UserDTO.from(order.getUser());
        }
        return dto;
    }

    // Inner DTO for order items
    public static class OrderItemDTO {
        private Long id;
        private String productName;
        private double price;
        private int quantity;
        private Long sellerId;

        public static OrderItemDTO from(OrderItem item) {
            OrderItemDTO dto = new OrderItemDTO();
            dto.id = item.getId();
            dto.productName = item.getProductName();
            dto.price = item.getPrice();
            dto.quantity = item.getQuantity();
            dto.sellerId = item.getSellerId();
            return dto;
        }

        public Long getId() { return id; }
        public String getProductName() { return productName; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public Long getSellerId() { return sellerId; }
    }

    public Long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getStatus() { return status; }
    public double getSubtotal() { return subtotal; }
    public double getTaxAmount() { return taxAmount; }
    public double getTotalAmount() { return totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItemDTO> getItems() { return items; }
    public UserDTO getUser() { return user; }
}
