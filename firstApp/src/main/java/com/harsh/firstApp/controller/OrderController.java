package com.harsh.firstApp.controller;


import com.harsh.firstApp.model.Cart;
import com.harsh.firstApp.model.Order;
import com.harsh.firstApp.model.Product;
import com.harsh.firstApp.repository.CartRepository;
import com.harsh.firstApp.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderController(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    // Place an order from a cart
    @PostMapping("/checkout/{cartId}")
    public Order checkout(@PathVariable Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart == null) return null;

        List<Product> products = cart.getProducts();
        double total = products.stream().mapToDouble(Product::getPrice).sum();

        Order order = new Order(products, total);
        return orderRepository.save(order);
    }

    // Get all orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get order by ID
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id).orElse(null);
    }
}
