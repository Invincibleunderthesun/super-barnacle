package com.harsh.firstApp.service;

import com.harsh.firstApp.exception.ApiException;
import com.harsh.firstApp.exception.InsufficientStockException;
import com.harsh.firstApp.model.*;
import com.harsh.firstApp.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository,
            CartRepository cartRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            AddressRepository addressRepository,
            EmailService emailService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.emailService = emailService;
    }

    /**
     * Checkout: creates an order from the user's cart with quantity-aware stock deduction,
     * shipping address snapshot, and 18% GST calculation.
     */
    public Order checkoutCart(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new ApiException("Cart is empty", HttpStatus.BAD_REQUEST);
        }

        // Validate shipping address
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ApiException("Address not found or doesn't belong to you", HttpStatus.NOT_FOUND));

        // Check stock for all items
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(product.getName(), product.getStock(), cartItem.getQuantity());
            }
        }

        // Calculate subtotal (before tax)
        double subtotal = cart.getItems().stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();

        // Create order with GST (constructor calculates tax)
        Order order = new Order(subtotal);
        order.setUser(user);
        order.setStatus(Order.Status.PAYMENT_PENDING);
        order.setShippingAddressFrom(address);
        order.setInvoiceNumber(generateInvoiceNumber());

        // Create OrderItem snapshots and deduct stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity());
            order.getItems().add(orderItem);
        }

        // Status history
        OrderStatusHistory history = new OrderStatusHistory(Order.Status.PAYMENT_PENDING.name(), order);
        order.getStatusHistory().add(history);

        orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        // Send order placed email
        if (user.getEmail() != null) {
            emailService.sendOrderConfirmationEmail(user.getEmail(), order.getId(), order.getTotalAmount());
        }

        logger.info("Order #{} created for user {} — subtotal ₹{}, GST ₹{}, total ₹{}",
                order.getId(), userId, order.getSubtotal(), order.getTaxAmount(), order.getTotalAmount());
        return order;
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public List<Order> getAllOrdersList() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersByUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        return orderRepository.findByUser(user);
    }

    /**
     * Update order status and send email notification.
     */
    public Order updateOrderStatus(Long orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return null;

        Order.Status previousStatus = order.getStatus();
        order.setStatus(status);

        OrderStatusHistory history = new OrderStatusHistory(status.name(), order);
        order.getStatusHistory().add(history);

        Order saved = orderRepository.save(order);

        // Send status update email
        if (order.getUser() != null && order.getUser().getEmail() != null) {
            emailService.sendOrderStatusEmail(order.getUser().getEmail(), orderId, status.name());
        }

        logger.info("Order {} status: {} → {}", orderId, previousStatus, status);
        return saved;
    }

    public List<Order> getOrdersByStatus(Long userId, Order.Status status) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        return orderRepository.findByUserAndStatus(user, status);
    }

    /**
     * Cancel order with stock restoration and email notification.
     */
    public Order cancelOrder(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return null;

        if (!isAdmin && (order.getUser() == null || !order.getUser().getId().equals(userId))) {
            logger.warn("Unauthorized cancel attempt: user={}, order={}", userId, orderId);
            return null;
        }

        if (order.getStatus() == Order.Status.PAYMENT_PENDING ||
                order.getStatus() == Order.Status.PENDING ||
                order.getStatus() == Order.Status.PAID) {

            order.setStatus(Order.Status.CANCELLED);

            // Restore stock
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }

            OrderStatusHistory history = new OrderStatusHistory(Order.Status.CANCELLED.name(), order);
            order.getStatusHistory().add(history);

            Order saved = orderRepository.save(order);

            // Send cancellation email
            if (order.getUser() != null && order.getUser().getEmail() != null) {
                emailService.sendOrderStatusEmail(order.getUser().getEmail(), orderId, "CANCELLED");
            }

            logger.info("Order {} cancelled. Stock restored for {} items.", orderId, order.getItems().size());
            return saved;
        }

        logger.warn("Cannot cancel order {} — status is {}", orderId, order.getStatus());
        return null;
    }

    public List<OrderStatusHistory> getOrderHistory(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return null;

        if (!isAdmin && (order.getUser() == null || !order.getUser().getId().equals(userId))) {
            return null;
        }

        return order.getStatusHistory();
    }

    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Object[]> statusStats = orderRepository.getOrderStatsByStatus();

        long totalOrders = 0;
        double totalRevenue = 0;

        for (Object[] row : statusStats) {
            Order.Status status = (Order.Status) row[0];
            long count = (Long) row[1];
            double amount = (Double) row[2];

            totalOrders += count;

            switch (status) {
                case PAYMENT_PENDING -> stats.put("paymentPendingOrders", count);
                case PENDING -> stats.put("pendingOrders", count);
                case PAID -> {
                    stats.put("paidOrders", count);
                    totalRevenue += amount;
                }
                case SHIPPED -> {
                    stats.put("shippedOrders", count);
                    totalRevenue += amount;
                }
                case DELIVERED -> {
                    stats.put("deliveredOrders", count);
                    totalRevenue += amount;
                }
                case CANCELLED -> stats.put("cancelledOrders", count);
            }
        }

        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);

        stats.putIfAbsent("paymentPendingOrders", 0L);
        stats.putIfAbsent("pendingOrders", 0L);
        stats.putIfAbsent("paidOrders", 0L);
        stats.putIfAbsent("shippedOrders", 0L);
        stats.putIfAbsent("deliveredOrders", 0L);
        stats.putIfAbsent("cancelledOrders", 0L);

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        stats.put("ordersLast7Days", orderRepository.countOrdersSince(weekAgo));

        return stats;
    }

    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByOrderDateBetween(start, end);
    }

    /**
     * Generate a unique invoice number: INV-YYYYMMDD-XXXXX
     */
    private String generateInvoiceNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.count() + 1;
        return String.format("INV-%s-%05d", datePart, count);
    }
}