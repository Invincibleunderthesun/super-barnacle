package com.harsh.firstApp.controller;


import com.harsh.firstApp.model.Cart;
import com.harsh.firstApp.model.Product;
import com.harsh.firstApp.repository.CartRepository;
import com.harsh.firstApp.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartController(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    // Create a new cart
    @PostMapping
    public Cart createCart() {
        return cartRepository.save(new Cart());
    }

    // Get cart by ID
    @GetMapping("/{id}")
    public Cart getCart(@PathVariable Long id) {
        return cartRepository.findById(id).orElse(null);
    }

    // Add product to cart
    @PostMapping("/{cartId}/add/{productId}")
    public Cart addProductToCart(@PathVariable Long cartId, @PathVariable Long productId) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if (cart != null && product != null) {
            cart.getProducts().add(product);
            return cartRepository.save(cart);
        }
        return null;
    }

    // Remove product from cart
    @DeleteMapping("/{cartId}/remove/{productId}")
    public Cart removeProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if (cart != null && product != null) {
            cart.getProducts().remove(product);
            return cartRepository.save(cart);
        }
        return null;
    }
}
