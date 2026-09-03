package com.harsh.firstApp.controller;

import com.harsh.firstApp.dto.ApiResponse;
import com.harsh.firstApp.exception.ApiException;
import com.harsh.firstApp.exception.ResourceNotFoundException;
import com.harsh.firstApp.model.Order;
import com.harsh.firstApp.model.OrderStatusHistory;
import com.harsh.firstApp.security.JwtFilter;
import com.harsh.firstApp.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management APIs")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final JwtFilter jwtFilter;

    public OrderController(OrderService orderService, JwtFilter jwtFilter) {
        this.orderService = orderService;
        this.jwtFilter = jwtFilter;
    }

    /**
     * Verify the authenticated user matches the requested userId, or is admin.
     */
    private void verifyOwnership(Long userId) {
        if (JwtFilter.isAdmin()) return;
        Long currentUserId = jwtFilter.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(userId)) {
            throw new ApiException("Access denied: you can only access your own orders",
                    HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }
    }

    @Operation(summary = "Checkout user's cart with shipping address")
    @PostMapping("/checkout/user/{userId}")
    public ResponseEntity<ApiResponse<Order>> checkoutCart(
            @PathVariable Long userId,
            @RequestParam Long addressId) {
        verifyOwnership(userId);
        Order order = orderService.checkoutCart(userId, addressId);
        if (order == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Checkout failed. Cart is empty or user not found."));
        }
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", order));
    }

    @Operation(summary = "Get all orders with pagination (Admin only)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Order>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            throw new ResourceNotFoundException("Order", id);
        }
        // Verify ownership unless admin
        if (!JwtFilter.isAdmin() && order.getUser() != null) {
            verifyOwnership(order.getUser().getId());
        }
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }

    @Operation(summary = "Get orders by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByUser(@PathVariable Long userId) {
        verifyOwnership(userId);
        List<Order> orders = orderService.getOrdersByUser(userId);
        if (orders == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }

    @Operation(summary = "Update order status (Admin only)")
    @PutMapping("/{orderId}/status/{status}")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long orderId,
            @PathVariable Order.Status status) {
        Order order = orderService.updateOrderStatus(orderId, status);
        if (order == null) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }

    @Operation(summary = "Get orders by status for a user")
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByStatus(
            @PathVariable Long userId,
            @PathVariable Order.Status status) {
        verifyOwnership(userId);
        List<Order> orders = orderService.getOrdersByStatus(userId, status);
        if (orders == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }

    @Operation(summary = "Cancel order as user")
    @DeleteMapping("/{orderId}/cancel/user/{userId}")
    public ResponseEntity<ApiResponse<Order>> cancelOrderAsUser(
            @PathVariable Long orderId,
            @PathVariable Long userId) {
        verifyOwnership(userId);
        Order order = orderService.cancelOrder(orderId, userId, false);
        if (order == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Cannot cancel order. Order not found or not cancellable."));
        }
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", order));
    }

    @Operation(summary = "Cancel order as admin")
    @DeleteMapping("/{orderId}/cancel/admin")
    public ResponseEntity<ApiResponse<Order>> cancelOrderAsAdmin(@PathVariable Long orderId) {
        Order order = orderService.cancelOrder(orderId, null, true);
        if (order == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Cannot cancel order. Order not found or not cancellable."));
        }
        return ResponseEntity.ok(ApiResponse.success("Order cancelled by admin", order));
    }

    @Operation(summary = "Get order history as user")
    @GetMapping("/{orderId}/history/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderStatusHistory>>> getOrderHistoryAsUser(
            @PathVariable Long orderId,
            @PathVariable Long userId) {
        verifyOwnership(userId);
        List<OrderStatusHistory> history = orderService.getOrderHistory(orderId, userId, false);
        if (history == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved", history));
    }

    @Operation(summary = "Get order history as admin")
    @GetMapping("/{orderId}/history/admin")
    public ResponseEntity<ApiResponse<List<OrderStatusHistory>>> getOrderHistoryAsAdmin(
            @PathVariable Long orderId) {
        List<OrderStatusHistory> history = orderService.getOrderHistory(orderId, null, true);
        if (history == null) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved", history));
    }

    @Operation(summary = "Get order statistics (Admin only)")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getOrderStats() {
        var stats = orderService.getOrderStatistics();
        return ResponseEntity.ok(ApiResponse.success("Order statistics", stats));
    }
}